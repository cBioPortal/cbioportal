/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This file is part of cBioPortal.
 *
 * cBioPortal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package org.mskcc.cbio.portal.dao;

import org.mskcc.cbio.portal.model.*;
import org.apache.commons.lang.StringUtils;

import java.sql.*;
import java.util.*;

/**
 *
 * @author jgao
 */
public final class DaoCopyNumberSegment {

    private static final double FRACTION_GENOME_ALTERED_CUTOFF = 0.2;
    private static final String FRACTION_GENOME_ALTERED_ATTR_ID = "FRACTION_GENOME_ALTERED";

    private DaoCopyNumberSegment() {}
    
    public static int addCopyNumberSegment(CopyNumberSegment seg) throws DaoException {
        if (!MySQLbulkLoader.isBulkLoad()) {
            throw new DaoException("You have to turn on MySQLbulkLoader in order to insert mutations");
        } else {
            MySQLbulkLoader.getMySQLbulkLoader("copy_number_seg").insertRecord(
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

    public static void createFractionGenomeAlteredClinicalData(int cancerStudyId) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoCopyNumberSegment.class);
            pstmt = con.prepareStatement(
                    "SELECT `SAMPLE_ID`, IF((SELECT SUM(`END`-`START`) FROM copy_number_seg " + 
                    "AS c2 WHERE c2.`CANCER_STUDY_ID` = c1.`CANCER_STUDY_ID` AND c2.`SAMPLE_ID` = c1.`SAMPLE_ID` AND " + 
                    "ABS(c2.`SEGMENT_MEAN`) >= 0.2) IS NULL, 0, (SELECT SUM(`END`-`START`) FROM copy_number_seg " + 
                    "AS c2 WHERE c2.`CANCER_STUDY_ID` = c1.`CANCER_STUDY_ID` AND c2.`SAMPLE_ID` = c1.`SAMPLE_ID` AND " + 
                    "ABS(c2.`SEGMENT_MEAN`) >= 0.2) / SUM(`END`-`START`)) AS `VALUE` FROM `copy_number_seg` AS c1 , `cancer_study` " +
                    "WHERE c1.`CANCER_STUDY_ID` = cancer_study.`CANCER_STUDY_ID` AND cancer_study.`CANCER_STUDY_ID`=? " +
                    "GROUP BY cancer_study.`CANCER_STUDY_ID` , `SAMPLE_ID` HAVING SUM(`END`-`START`) > 0;");
            pstmt.setInt(1, cancerStudyId);
            Map<Integer, String> fractionGenomeAltereds = new HashMap<Integer, String>();
            rs = pstmt.executeQuery();
            while (rs.next()) {
                fractionGenomeAltereds.put(rs.getInt(1), rs.getString(2));
            }

            ClinicalAttribute clinicalAttribute = DaoClinicalAttributeMeta.getDatum(FRACTION_GENOME_ALTERED_ATTR_ID, cancerStudyId);
            if (clinicalAttribute == null) {
                ClinicalAttribute attr = new ClinicalAttribute(FRACTION_GENOME_ALTERED_ATTR_ID, "Fraction Genome Altered", "Fraction Genome Altered", "NUMBER",
                    false, "20", cancerStudyId);
                DaoClinicalAttributeMeta.addDatum(attr);
            }
            
            for (Map.Entry<Integer, String> fractionGenomeAltered : fractionGenomeAltereds.entrySet()) {
                DaoClinicalData.addSampleDatum(fractionGenomeAltered.getKey(), FRACTION_GENOME_ALTERED_ATTR_ID, fractionGenomeAltered.getValue());
            }
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoCopyNumberSegment.class, con, pstmt, rs);
        }
    }
    
    public static long getLargestId() throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoMutation.class);
            pstmt = con.prepareStatement
                    ("SELECT MAX(`SEG_ID`) FROM `copy_number_seg`");
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
        if (sampleIds.isEmpty()) {
            return Collections.emptyList();
        }
        String concatSampleIds = "('"+StringUtils.join(sampleIds, "','")+"')";
        
        List<CopyNumberSegment> segs = new ArrayList<CopyNumberSegment>();
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoCopyNumberSegment.class);
            pstmt = con.prepareStatement
                    ("SELECT * FROM copy_number_seg"
                    + " WHERE `SAMPLE_ID` IN "+ concatSampleIds
                    + " AND `CANCER_STUDY_ID`="+cancerStudyId);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                CopyNumberSegment seg = new CopyNumberSegment(
                        rs.getInt("CANCER_STUDY_ID"),
                        rs.getInt("SAMPLE_ID"),
                        rs.getString("CHR"),
                        rs.getLong("START"),
                        rs.getLong("END"),
                        rs.getInt("NUM_PROBES"),
                        rs.getDouble("SEGMENT_MEAN"));
                seg.setSegId(rs.getLong("SEG_ID"));
                segs.add(seg);
            }
            return segs;
        } catch (NullPointerException e) {
            throw new DaoException(e);
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoCopyNumberSegment.class, con, pstmt, rs);
        }
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
        Map<Integer,Long> map = new HashMap<Integer,Long>();
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        String sql;
        try {
            con = JdbcUtil.getDbConnection(DaoCopyNumberSegment.class);
            if (cutoff>0) {
                sql = "SELECT  `SAMPLE_ID`, SUM(`END`-`START`)"
                    + " FROM `copy_number_seg`"
                    + " WHERE `CANCER_STUDY_ID`="+cancerStudyId
                    + " AND ABS(`SEGMENT_MEAN`)>=" + cutoff
                    + " AND `SAMPLE_ID` IN ('" + StringUtils.join(sampleIds,"','") +"')"
                    + " GROUP BY `SAMPLE_ID`";
            } else {
                sql = "SELECT  `SAMPLE_ID`, SUM(`END`-`START`)"
                    + " FROM `copy_number_seg`"
                    + " WHERE `CANCER_STUDY_ID`="+cancerStudyId
                    + " AND `SAMPLE_ID` IN ('" + StringUtils.join(sampleIds,"','") +"')"
                    + " GROUP BY `SAMPLE_ID`";
            }
            
            pstmt = con.prepareStatement(sql);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                map.put(rs.getInt(1), rs.getLong(2));
            }
            
            return map;
        } catch (NullPointerException e) {
            throw new DaoException(e);
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoCopyNumberSegment.class, con, pstmt, rs);
        }
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
            pstmt = con.prepareStatement("SELECT EXISTS (SELECT 1 FROM `copy_number_seg` WHERE `CANCER_STUDY_ID`=?)");
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
                + " WHERE `CANCER_STUDY_ID`=? AND `SAMPLE_ID`=?");
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
}
