/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.mskcc.cbio.portal.dao;

import java.sql.*;
import java.util.*;
import org.mskcc.cbio.portal.model.ClinicalAttribute;
import org.mskcc.cbio.portal.model.CopyNumberSegment;

/**
 *
 * @author jgao
 */
public final class DaoCopyNumberSegment {

    private static final String FRACTION_GENOME_ALTERED_ATTR_ID = "FRACTION_GENOME_ALTERED";

    private DaoCopyNumberSegment() {}
    
    public static int addCopyNumberSegment(CopyNumberSegment seg) throws DaoException {
        if (!ClickHouseBulkLoader.isBulkLoad()) {
            throw new DaoException("You have to turn on ClickHouseBulkLoader in order to insert mutations");
        } else {
            ClickHouseBulkLoader.getClickHouseBulkLoader("copy_number_seg").insertRecord(
                    Long.toString(seg.getSegId()),
                    Integer.toString(seg.getCancerStudyId()),
                    Integer.toString(seg.getSampleId()),
                    seg.getChr(),
                    Long.toString(seg.getStart()),
                    Long.toString(seg.getEnd()),
                    Integer.toString(seg.getNumProbes()),
                    Double.toString(seg.getSegMean())
            );
            return 1;
        }
    }

    /**
     * Ensures FRACTION_GENOME_ALTERED clinical sample attribute is created and up to date.
     * @param cancerStudyId - id of the study to create the clinical attribute for
     * @param fractionGenomeAltereds - values by sample id, computed by the importer from the
     *                  segments it read (see FractionGenomeAlteredCalculator) rather than
     *                  read back from copy_number_seg, which may not yet be visible on
     *                  every ClickHouse replica
     * @param updateMode -  if true, updates the attribute if it exists
     * @throws DaoException
     */

    public static void createFractionGenomeAlteredClinicalData(int cancerStudyId, Map<Integer, String> fractionGenomeAltereds, boolean updateMode) throws DaoException {
        if (!ClickHouseBulkLoader.isBulkLoad()) {
            throw new DaoException("You have to turn on ClickHouseBulkLoader in order to insert Fraction Genome Altered");
        }

        // Flush pending segment inserts before writing clinical data
        ClickHouseBulkLoader.flushAll();

        ClinicalAttribute clinicalAttribute = DaoClinicalAttributeMeta.getDatum(FRACTION_GENOME_ALTERED_ATTR_ID, cancerStudyId);
        if (clinicalAttribute == null) {
            ClinicalAttribute attr = new ClinicalAttribute(FRACTION_GENOME_ALTERED_ATTR_ID, "Fraction Genome Altered", "Fraction Genome Altered", "NUMBER",
                false, "20", cancerStudyId);
            DaoClinicalAttributeMeta.addDatum(attr);
        }

        if (updateMode) {
            DaoClinicalData.removeSampleAttributesData(fractionGenomeAltereds.keySet(), FRACTION_GENOME_ALTERED_ATTR_ID);
        }
        for (Map.Entry<Integer, String> fractionGenomeAltered : fractionGenomeAltereds.entrySet()) {
            DaoClinicalData.addSampleDatum(fractionGenomeAltered.getKey(), FRACTION_GENOME_ALTERED_ATTR_ID, fractionGenomeAltered.getValue());
        }

        // Necessary for deduplication
        ClickHouseOptimizer.optimizeTables("clinical_sample");
    }
    
    public static long getLargestId() throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoMutation.class);
            pstmt = con.prepareStatement
                    ("SELECT max(`seg_id`) FROM `copy_number_seg`");
            rs = pstmt.executeQuery();
            return rs.next() ? rs.getLong(1) : 0;
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoMutation.class, con, pstmt, rs);
        }
    }
    
    public static List<CopyNumberSegment> getSegmentForASample(
            int sampleId, int cancerStudyId) throws DaoException {
        return getSegmentForSamples(Collections.singleton(sampleId),cancerStudyId);
    }
    
    public static List<CopyNumberSegment> getSegmentForSamples(
            Collection<Integer> sampleIds, int cancerStudyId) throws DaoException {
        if (sampleIds == null || sampleIds.isEmpty()) {
            return Collections.emptyList();
        }
        return ClickHouseBulkUploader.upload(sampleIds, stagingTable -> {
            List<CopyNumberSegment> segs = new ArrayList<>();
            Connection con = null;
            try {
                con = JdbcUtil.getDbConnection(DaoCopyNumberSegment.class);
                try (PreparedStatement pstmt = con.prepareStatement(
                        "SELECT * FROM copy_number_seg" +
                        " WHERE `sample_id` IN (SELECT id FROM " + stagingTable + ")" +
                        " AND `cancer_study_id`=" + cancerStudyId);
                     ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        CopyNumberSegment seg = new CopyNumberSegment(
                                rs.getInt("cancer_study_id"),
                                rs.getInt("sample_id"),
                                rs.getString("chr"),
                                rs.getLong("start"),
                                rs.getLong("end"),
                                rs.getInt("num_probes"),
                                rs.getDouble("segment_mean"));
                        seg.setSegId(rs.getLong("seg_id"));
                        segs.add(seg);
                    }
                }
                return segs;
            } finally {
                JdbcUtil.closeAll(DaoCopyNumberSegment.class, con, null, null);
            }
        });
    }
    
    public static double getCopyNumberActeredFraction(int sampleId,
            int cancerStudyId, double cutoff) throws DaoException {
        Double d = getCopyNumberActeredFraction(Collections.singleton(sampleId), cancerStudyId, cutoff)
                .get(sampleId);
        return d==null ? Double.NaN : d;
    }
    
    public static Map<Integer,Double> getCopyNumberActeredFraction(Collection<Integer> sampleIds,
            int cancerStudyId, double cutoff) throws DaoException {
        Map<Integer,Long> alteredLength = getCopyNumberAlteredLength(sampleIds, cancerStudyId, cutoff);
        Map<Integer,Long> measuredLength = getCopyNumberAlteredLength(sampleIds, cancerStudyId, 0);
        Map<Integer,Double> fraction = new HashMap<Integer,Double>(alteredLength.size());
        for (Integer sampleId : sampleIds) {
            Long ml = measuredLength.get(sampleId);
            if (ml==null || ml==0) {
                continue;
            }
            Long al = alteredLength.get(sampleId);
            if (al==null) {
                al = (long) 0;
            }
            fraction.put(sampleId, 1.0*al/ml);
        }
        return fraction;
    }
    
    private static Map<Integer,Long> getCopyNumberAlteredLength(Collection<Integer> sampleIds,
            int cancerStudyId, double cutoff) throws DaoException {
        return ClickHouseBulkUploader.upload(sampleIds, stagingTable -> {
            String cutoffClause = cutoff > 0 ? " AND abs(`segment_mean`)>=" + cutoff : "";
            String sql = "SELECT `sample_id`, sum(`end`-`start`)" +
                    " FROM `copy_number_seg`" +
                    " WHERE `cancer_study_id`=" + cancerStudyId +
                    cutoffClause +
                    " AND `sample_id` IN (SELECT id FROM " + stagingTable + ")" +
                    " GROUP BY `sample_id`";
            Map<Integer,Long> map = new HashMap<>();
            Connection con = null;
            try {
                con = JdbcUtil.getDbConnection(DaoCopyNumberSegment.class);
                try (PreparedStatement pstmt = con.prepareStatement(sql);
                     ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        map.put(rs.getInt(1), rs.getLong(2));
                    }
                }
                return map;
            } finally {
                JdbcUtil.closeAll(DaoCopyNumberSegment.class, con, null, null);
            }
        });
    }
    
    /**
     * 
     * @param cancerStudyId
     * @return true if segment data exist for the cancer study
     * @throws DaoException 
     */
    public static boolean segmentDataExistForCancerStudy(int cancerStudyId) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoCopyNumberSegment.class);
            pstmt = con.prepareStatement("SELECT EXISTS (SELECT 1 FROM `copy_number_seg` WHERE `cancer_study_id`=?)");
            pstmt.setInt(1, cancerStudyId);
            rs = pstmt.executeQuery();
            return rs.next() && rs.getInt(1)==1;
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoCopyNumberSegment.class, con, pstmt, rs);
        }
    }
    
    /**
     * 
     * @param cancerStudyId
     * @param sampleId
     * @return true if segment data exist for the case
     * @throws DaoException 
     */
    public static boolean segmentDataExistForSample(int cancerStudyId, int sampleId) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoCopyNumberSegment.class);
            pstmt = con.prepareStatement("SELECT EXISTS(SELECT 1 FROM `copy_number_seg`"
                + " WHERE `cancer_study_id`=? AND `sample_id`=?");
            pstmt.setInt(1, cancerStudyId);
            pstmt.setInt(2, sampleId);
            rs = pstmt.executeQuery();
            return rs.next() && rs.getInt(1)==1;
        } catch (NullPointerException e) {
            throw new DaoException(e);
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoCopyNumberSegment.class, con, pstmt, rs);
        }
    }

    public static void  deleteSegmentDataForSamples(int cancerStudyId, Set<Integer> sampleIds) throws DaoException {
        if (sampleIds.isEmpty()) {
            return;
        }
        ClickHouseBulkUploader.upload(sampleIds, stagingTable -> {
            Connection con = null;
            try {
                con = JdbcUtil.getDbConnection(DaoCopyNumberSegment.class);
                try (PreparedStatement pstmt = con.prepareStatement(
                        "DELETE FROM `copy_number_seg` WHERE `cancer_study_id`=? AND `sample_id` IN (SELECT id FROM " + stagingTable + ")")) {
                    pstmt.setInt(1, cancerStudyId);
                    pstmt.executeUpdate();
                }
            } finally {
                JdbcUtil.closeAll(DaoCopyNumberSegment.class, con, null, null);
            }
            return null;
        });
    }
}
