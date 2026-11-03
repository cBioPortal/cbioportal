/*
 * Copyright (c) 2015 - 2022 Memorial Sloan Kettering Cancer Center.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.mskcc.cbio.portal.dao;

import org.apache.commons.collections4.MapIterator;
import org.apache.commons.collections4.keyvalue.MultiKey;
import org.apache.commons.collections4.map.MultiKeyMap;
import org.apache.commons.collections4.MapIterator;
import org.apache.commons.lang3.StringUtils;
import org.mskcc.cbio.portal.model.CancerStudy;
import org.mskcc.cbio.portal.model.CanonicalGene;
import org.mskcc.cbio.portal.model.ClinicalAttribute;
import org.mskcc.cbio.portal.model.ExtendedMutation;
import org.mskcc.cbio.portal.model.shared.GeneticAlterationType;
import org.mskcc.cbio.portal.model.GeneticProfile;
import org.mskcc.cbio.portal.model.Sample;
import org.mskcc.cbio.portal.util.MutationKeywordUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Data access object for Mutation table
 */
public final class DaoMutation {
    private static final Logger LOG = LoggerFactory.getLogger(DaoMutation.class);
    public static final String NAN = "NaN";
    private static final String MUTATION_COUNT_ATTR_ID = "MUTATION_COUNT";
    private static final String DELETE_ALTERATION_DRIVER_ANNOTATION = "DELETE from alteration_driver_annotation WHERE genetic_profile_id=? and sample_id=?";
    private static final String DELETE_MUTATION = "DELETE from mutation WHERE genetic_profile_id=? and sample_id=?";

    public static int addMutation(ExtendedMutation mutation, boolean newMutationEvent) throws DaoException {
        if (!ClickHouseBulkLoader.isBulkLoad()) {
            throw new DaoException("You have to turn on ClickHouseBulkLoader in order to insert mutations");
        } else {
            int result = 1;
            if (newMutationEvent) {
                //add event first, as mutation has a Foreign key constraint to the event:
                result = addMutationEvent(mutation.getEvent())+1;
            }

            if ((mutation.getDriverFilter() != null
                && !mutation.getDriverFilter().isEmpty()
                && !mutation.getDriverFilter().toLowerCase().equals("na"))
                ||
                (mutation.getDriverTiersFilter() != null
                && !mutation.getDriverTiersFilter().isEmpty()
                && !mutation.getDriverTiersFilter().toLowerCase().equals("na"))) {
                ClickHouseBulkLoader.getClickHouseBulkLoader("alteration_driver_annotation").insertRecord(
                    Long.toString(mutation.getMutationEventId()),
                    Integer.toString(mutation.getGeneticProfileId()),
                    Integer.toString(mutation.getSampleId()),
                    mutation.getDriverFilter(),
                    mutation.getDriverFilterAnn(),
                    mutation.getDriverTiersFilter(),
                    mutation.getDriverTiersFilterAnn()
                );
            }

            ClickHouseBulkLoader.getClickHouseBulkLoader("mutation").insertRecord(
                    Long.toString(mutation.getMutationEventId()),
                    Integer.toString(mutation.getGeneticProfileId()),
                    Integer.toString(mutation.getSampleId()),
                    Long.toString(mutation.getGene().getEntrezGeneId()),
                    mutation.getSequencingCenter(),
                    mutation.getSequencer(),
                    mutation.getMutationStatus(),
                    mutation.getValidationStatus(),
                    mutation.getTumorSeqAllele1(),
                    mutation.getTumorSeqAllele2(),
                    mutation.getMatchedNormSampleBarcode(),
                    mutation.getMatchNormSeqAllele1(),
                    mutation.getMatchNormSeqAllele2(),
                    mutation.getTumorValidationAllele1(),
                    mutation.getTumorValidationAllele2(),
                    mutation.getMatchNormValidationAllele1(),
                    mutation.getMatchNormValidationAllele2(),
                    mutation.getVerificationStatus(),
                    mutation.getSequencingPhase(),
                    mutation.getSequenceSource(),
                    mutation.getValidationMethod(),
                    mutation.getScore(),
                    mutation.getBamFile(),
                    (mutation.getTumorAltCount() == null) ? null : Integer.toString(mutation.getTumorAltCount()),
                    (mutation.getTumorRefCount() == null) ? null : Integer.toString(mutation.getTumorRefCount()),
                    (mutation.getNormalAltCount() == null) ? null : Integer.toString(mutation.getNormalAltCount()),
                    (mutation.getNormalRefCount() == null) ? null : Integer.toString(mutation.getNormalRefCount()),
                    //AminoAcidChange column is not used
                    null,
                    mutation.getAnnotationJson());
            return result;
        }
    }

    public static int addMutationEvent(ExtendedMutation.MutationEvent event) throws DaoException {
        // use this code if bulk loading
        // write to the temp file maintained by the ClickHouseBulkLoader
        String keyword = MutationKeywordUtils.guessOncotatorMutationKeyword(event.getProteinChange(), event.getMutationType());
        ClickHouseBulkLoader.getClickHouseBulkLoader("mutation_event").insertRecord(
                Long.toString(event.getMutationEventId()),
                Long.toString(event.getGene().getEntrezGeneId()),
                event.getChr(),
                Long.toString(event.getStartPosition()),
                Long.toString(event.getEndPosition()),
                event.getReferenceAllele(),
                event.getTumorSeqAllele(),
                event.getProteinChange(),
                event.getMutationType(),
                event.getNcbiBuild(),
                event.getStrand(),
                event.getVariantType(),
                event.getDbSnpRs(),
                event.getDbSnpValStatus(),
                event.getRefseqMrnaId(),
                event.getCodonChange(),
                event.getUniprotAccession(),
                Integer.toString(event.getProteinPosStart()),
                Integer.toString(event.getProteinPosEnd()),
                boolToStr(event.isCanonicalTranscript()),
                keyword==null ? "\\N":(event.getGene().getHugoGeneSymbolAllCaps()+" "+keyword));
        return 1;
    }

    public static void createMutationCountClinicalData(GeneticProfile geneticProfile) throws DaoException {
        // Make sure we are computing MUTATION_COUNT based off the latest changes to each table
        if (ClickHouseBulkLoader.isBulkLoad()) {
            ClickHouseBulkLoader.flushAll();
        }

        Connection con = null;
        PreparedStatement selectStmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoMutation.class);
            // add mutation count meta attribute if it does not exist
            ClinicalAttribute clinicalAttribute = DaoClinicalAttributeMeta.getDatum(MUTATION_COUNT_ATTR_ID, geneticProfile.getCancerStudyId());
            if (clinicalAttribute == null) {
                ClinicalAttribute attr = new ClinicalAttribute(MUTATION_COUNT_ATTR_ID, "Mutation Count", "Mutation Count", "NUMBER",
                    false, "30", geneticProfile.getCancerStudyId());
                DaoClinicalAttributeMeta.addDatum(attr);
            }

            LOG.info("createMutationCountClinicalData (bulk insert) for profile {}", geneticProfile.getGeneticProfileId());
            // SELECT mutation counts into a map, then insert via bulk loader
            // No prior DELETE needed: clinical_sample is ReplacingMergeTree ORDER BY (internal_id, attr_id),
            // so OPTIMIZE TABLE FINAL will keep the latest row per key, replacing any stale values.
            // Issuing a DELETE FROM before INSERT causes a lightweight-delete/OPTIMIZE race that silently
            // wipes the newly inserted rows.
            selectStmt = con.prepareStatement(
                "SELECT sample_profile.`sample_id`, count(DISTINCT mutation_event.`chr`, mutation_event.`start_position`, " +
                    "mutation_event.`end_position`, mutation_event.`reference_allele`, mutation_event.`tumor_seq_allele`) AS MUTATION_COUNT " +
                    "FROM `sample_profile` " +
                    "LEFT JOIN mutation ON mutation.`sample_id` = sample_profile.`sample_id` " +
                    "AND ( UPPER(mutation.`mutation_status`) <> 'GERMLINE' OR mutation.`mutation_status` IS NULL ) " +
                    "LEFT JOIN mutation_event ON mutation.`mutation_event_id` = mutation_event.`mutation_event_id` " +
                    "INNER JOIN genetic_profile ON genetic_profile.`genetic_profile_id` = sample_profile.`genetic_profile_id` " +
                    "WHERE genetic_profile.`genetic_alteration_type` = 'MUTATION_EXTENDED' " +
                    "AND genetic_profile.`genetic_profile_id`=? " +
                    "GROUP BY sample_profile.`genetic_profile_id`, sample_profile.`sample_id`"
            );
            selectStmt.setInt(1, geneticProfile.getGeneticProfileId());
            rs = selectStmt.executeQuery();
            Map<Integer, String> mutationCounts = new HashMap<>();
            while (rs.next()) {
                mutationCounts.put(rs.getInt(1), rs.getString(2));
            }

            LOG.info("inserting MUTATION_COUNT for {} samples", mutationCounts.size());
            for (Map.Entry<Integer, String> entry : mutationCounts.entrySet()) {
                DaoClinicalData.addSampleDatum(entry.getKey(), MUTATION_COUNT_ATTR_ID, entry.getValue());
            }

            // Flush inserts and force ReplacingMergeTree deduplication so the latest values are visible
            ClickHouseOptimizer.optimizeTables("clinical_sample");
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoMutation.class, con, selectStmt, rs);
        }
    }

    public static void calculateMutationCountByKeyword(int geneticProfileId) throws DaoException {
        if (!ClickHouseBulkLoader.isBulkLoad()) {
            throw new DaoException("You have to turn on ClickHouseBulkLoader in order to update mutation counts by keyword");
        } else {
            MultiKeyMap mutationEventKeywordCountMap = getMutationEventKeywordCountByGeneticProfileId(geneticProfileId); // mutation event keyword -> entrez id -> keyword count
            Map<Long, Integer> geneCountMap = getGeneCountByGeneticProfileId(geneticProfileId); // entrez id -> gene count
            MapIterator it = mutationEventKeywordCountMap.mapIterator();
            while (it.hasNext()) {
                it.next();
                MultiKey mk = (MultiKey) it.getKey();
                String mutationEventKeyword = String.valueOf(mk.getKey(0));
                Long entrezGeneId = Long.valueOf(mk.getKey(1).toString());
                String keywordCount = it.getValue().toString();
                Integer geneCount = geneCountMap.get(entrezGeneId);
                ClickHouseBulkLoader.getClickHouseBulkLoader("mutation_count_by_keyword").insertRecord(
                        Integer.toString(geneticProfileId),
                        mutationEventKeyword,
                        Long.toString(entrezGeneId),
                        keywordCount,
                        Integer.toString(geneCount)
                );
            }
        }
    }

    public static MultiKeyMap getMutationEventKeywordCountByGeneticProfileId(int geneticProfileId) throws DaoException {
        MultiKeyMap mutationEventKeywordCountByGeneticProfileId = new MultiKeyMap();
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoMutation.class);
            pstmt = con.prepareStatement(
                    "SELECT mutation_event.`keyword`, mutation_event.`entrez_gene_id`,  if(mutation_event.`keyword` IS NULL, 0, count(DISTINCT(mutation.sample_id))) AS keyword_count " +
                            "FROM mutation_event JOIN mutation on mutation.`mutation_event_id` = mutation_event.`mutation_event_id` " +
                            "WHERE mutation.`genetic_profile_id` = ? " +
                            "GROUP BY mutation_event.`keyword`, mutation_event.`entrez_gene_id`"
            );
            pstmt.setInt(1, geneticProfileId);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                mutationEventKeywordCountByGeneticProfileId.put(rs.getString(1), rs.getLong(2), rs.getInt(3));
            }
            return mutationEventKeywordCountByGeneticProfileId;
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoMutation.class, con, pstmt, rs);
        }
    }

    public static Map<Long, Integer> getGeneCountByGeneticProfileId(int geneticProfileId) throws DaoException {
        Map<Long, Integer> geneCountByGeneticProfileId = new HashMap<>();
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoMutation.class);
            pstmt = con.prepareStatement(
                "SELECT entrez_gene_id AS `entrez_gene_id`, count(DISTINCT(sample_id)) AS `gene_count`" +
                    " FROM mutation WHERE genetic_profile_id = ? " +
                    "GROUP BY entrez_gene_id");
            pstmt.setInt(1, geneticProfileId);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                geneCountByGeneticProfileId.put(rs.getLong("entrez_gene_id"), rs.getInt("gene_count"));
            }
            return geneCountByGeneticProfileId;
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoMutation.class, con, pstmt, rs);
        }
    }

    /**
     * Used by GeneticAlterationUtil (which is used by GetProfileData via webservice.do).  This use comes out of an
     * effort to discontinue the use of the business module.
     */
    public static ArrayList<ExtendedMutation> getMutations (int geneticProfileId, Collection<Integer> targetSampleList,
            long entrezGeneId) throws DaoException {
        return ClickHouseBulkUploader.upload(targetSampleList, stagingTable -> {
            ArrayList<ExtendedMutation> mutationList = new ArrayList<>();
            Connection con = null;
            try {
                con = JdbcUtil.getDbConnection(DaoMutation.class);
                String inClause = stagingTable == null ? "" : " AND sample_id IN (SELECT id FROM " + stagingTable + ")";
                try (PreparedStatement pstmt = con.prepareStatement(
                        "SELECT * FROM mutation " +
                        "INNER JOIN mutation_event ON mutation.mutation_event_id=mutation_event.mutation_event_id " +
                        "WHERE genetic_profile_id = ? AND mutation.entrez_gene_id = ?" + inClause)) {
                    pstmt.setInt(1, geneticProfileId);
                    pstmt.setLong(2, entrezGeneId);
                    try (ResultSet rs = pstmt.executeQuery()) {
                        while (rs.next()) {
                            mutationList.add(extractMutation(rs));
                        }
                    }
                }
                return mutationList;
            } finally {
                JdbcUtil.closeAll(DaoMutation.class, con, null, null);
            }
        });
    }

    /**
     * @deprecated  We believe that this method is no longer called by any part of the codebase, and it will soon be deleted.
     */
    @Deprecated
    public static ArrayList<ExtendedMutation> getMutations (int geneticProfileId, int sampleId,
                                                            long entrezGeneId) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        ArrayList <ExtendedMutation> mutationList = new ArrayList <ExtendedMutation>();
        try {
            con = JdbcUtil.getDbConnection(DaoMutation.class);
            pstmt = con.prepareStatement(
                    "SELECT * FROM mutation " +
                    "INNER JOIN mutation_event ON mutation.mutation_event_id=mutation_event.mutation_event_id " +
                    "WHERE sample_id = ? AND genetic_profile_id = ? AND mutation.entrez_gene_id = ?");
            pstmt.setInt(1, sampleId);
            pstmt.setInt(2, geneticProfileId);
            pstmt.setLong(3, entrezGeneId);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                ExtendedMutation mutation = extractMutation(rs);
                mutationList.add(mutation);
            }
        } catch (NullPointerException e) {
            throw new DaoException(e);
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoMutation.class, con, pstmt, rs);
        }
        return mutationList;
    }

    /**
     * Gets all Genes in a Specific Genetic Profile.
     *
     * @param geneticProfileId Genetic Profile ID.
     * @return Set of Canonical Genes.
     * @throws DaoException Database Error.
     */
    public static Set<CanonicalGene> getGenesInProfile(int geneticProfileId) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        Set<CanonicalGene> geneSet = new HashSet<CanonicalGene>();
        DaoGeneOptimized daoGene = DaoGeneOptimized.getInstance();
        try {
            con = JdbcUtil.getDbConnection(DaoMutation.class);
            pstmt = con.prepareStatement(
                    "SELECT DISTINCT entrez_gene_id FROM mutation WHERE genetic_profile_id = ?");
            pstmt.setInt(1, geneticProfileId);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                geneSet.add(daoGene.getGene(rs.getLong("entrez_gene_id")));
            }
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoMutation.class, con, pstmt, rs);
        }
        return geneSet;
    }

    /**
     * @deprecated  We believe that this method is no longer called by any part of the codebase, and it will soon be deleted.
     */
    @Deprecated
    public static ArrayList<ExtendedMutation> getMutations (long entrezGeneId) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        ArrayList <ExtendedMutation> mutationList = new ArrayList <ExtendedMutation>();
        try {
            con = JdbcUtil.getDbConnection(DaoMutation.class);
            pstmt = con.prepareStatement(
                    "SELECT * FROM mutation " +
                    "INNER JOIN mutation_event ON mutation.mutation_event_id=mutation_event.mutation_event_id " +
                    "WHERE mutation.entrez_gene_id = ?");
            pstmt.setLong(1, entrezGeneId);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                ExtendedMutation mutation = extractMutation(rs);
                mutationList.add(mutation);
            }
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoMutation.class, con, pstmt, rs);
        }
        return mutationList;
    }

    /**
     * @deprecated  We believe that this method is no longer called by any part of the codebase, and it will soon be deleted.
     */
    @Deprecated
    public static ArrayList<ExtendedMutation> getMutations (long entrezGeneId, String aminoAcidChange) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        ArrayList <ExtendedMutation> mutationList = new ArrayList <ExtendedMutation>();
        try {
            con = JdbcUtil.getDbConnection(DaoMutation.class);
            pstmt = con.prepareStatement(
                    "SELECT * FROM mutation_event " +
                    "INNER JOIN mutation ON mutation.mutation_event_id=mutation_event.mutation_event_id " +
                    "WHERE mutation.entrez_gene_id = ? AND protein_change = ?");
            pstmt.setLong(1, entrezGeneId);
            pstmt.setString(2, aminoAcidChange);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                ExtendedMutation mutation = extractMutation(rs);
                mutationList.add(mutation);
            }
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoMutation.class, con, pstmt, rs);
        }
        return mutationList;
    }

    public static ArrayList<ExtendedMutation> getMutations (int geneticProfileId, int sampleId) throws DaoException {
        return getMutations(geneticProfileId, Arrays.asList(Integer.valueOf(sampleId)));
    }

    public static ArrayList<ExtendedMutation> getMutations (int geneticProfileId, List<Integer> sampleIds) throws DaoException {
        return ClickHouseBulkUploader.upload(sampleIds, stagingTable -> {
            ArrayList<ExtendedMutation> mutationList = new ArrayList<>();
            Connection con = null;
            try {
                con = JdbcUtil.getDbConnection(DaoMutation.class);
                String inClause = stagingTable == null ? "" : " AND sample_id IN (SELECT id FROM " + stagingTable + ")";
                try (PreparedStatement pstmt = con.prepareStatement(
                        "SELECT * FROM mutation " +
                        "INNER JOIN mutation_event ON mutation.mutation_event_id=mutation_event.mutation_event_id " +
                        "WHERE genetic_profile_id = ?" + inClause)) {
                    pstmt.setInt(1, geneticProfileId);
                    try (ResultSet rs = pstmt.executeQuery()) {
                        while (rs.next()) {
                            mutationList.add(extractMutation(rs));
                        }
                    }
                }
                return mutationList;
            } finally {
                JdbcUtil.closeAll(DaoMutation.class, con, null, null);
            }
        });
    }

    /**
     * @deprecated  We believe that this method is no longer called by any part of the codebase, and it will soon be deleted.
     */
    @Deprecated
    public static boolean hasAlleleFrequencyData (int geneticProfileId, int sampleId) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoMutation.class);
            pstmt = con.prepareStatement(
                    "SELECT EXISTS (SELECT 1 FROM mutation " +
                    "WHERE genetic_profile_id = ? AND sample_id = ? AND tumor_alt_count>=0 AND tumor_ref_count>=0)");
            pstmt.setInt(1, geneticProfileId);
            pstmt.setInt(2, sampleId);
            rs = pstmt.executeQuery();
            return rs.next() && rs.getInt(1)==1;
        } catch (NullPointerException e) {
            throw new DaoException(e);
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoMutation.class, con, pstmt, rs);
        }
    }

    /**
     * @deprecated  We believe that this method is no longer called by any part of the codebase, and it will soon be deleted.
     */
    @Deprecated
    public static ArrayList<ExtendedMutation> getMutations (long entrezGeneId, String aminoAcidChange, int excludeSampleId) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        ArrayList <ExtendedMutation> mutationList = new ArrayList <ExtendedMutation>();
        try {
            con = JdbcUtil.getDbConnection(DaoMutation.class);
            pstmt = con.prepareStatement(
                    "SELECT * FROM mutation, mutation_event " +
                    "WHERE mutation.mutation_event_id=mutation_event.mutation_event_id " +
                    "AND mutation.entrez_gene_id = ? AND protein_change = ? AND sample_id <> ?");
            pstmt.setLong(1, entrezGeneId);
            pstmt.setString(2, aminoAcidChange);
            pstmt.setInt(3, excludeSampleId);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                ExtendedMutation mutation = extractMutation(rs);
                mutationList.add(mutation);
            }
        } catch (NullPointerException e) {
            throw new DaoException(e);
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoMutation.class, con, pstmt, rs);
        }
        return mutationList;
    }

    /**
     * Used by GetMutationData via webservice.do.  This use comes out of an effort to
     * discontinue the use of the business module.
     */
    public static ArrayList<ExtendedMutation> getMutations (int geneticProfileId,
            long entrezGeneId) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        ArrayList <ExtendedMutation> mutationList = new ArrayList <ExtendedMutation>();
        try {
            con = JdbcUtil.getDbConnection(DaoMutation.class);
            pstmt = con.prepareStatement(
                    "SELECT * FROM mutation " +
                    "INNER JOIN mutation_event ON mutation.mutation_event_id=mutation_event.mutation_event_id " +
                    "WHERE genetic_profile_id = ? AND mutation.entrez_gene_id = ?");
            pstmt.setInt(1, geneticProfileId);
            pstmt.setLong(2, entrezGeneId);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                ExtendedMutation mutation = extractMutation(rs);
                mutationList.add(mutation);
            }
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoMutation.class, con, pstmt, rs);
        }
        return mutationList;
    }

    public static ArrayList<ExtendedMutation> getAllMutations () throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        ArrayList <ExtendedMutation> mutationList = new ArrayList <ExtendedMutation>();
        try {
            con = JdbcUtil.getDbConnection(DaoMutation.class);
            pstmt = con.prepareStatement(
                    "SELECT * FROM mutation " +
                        "INNER JOIN mutation_event ON mutation.mutation_event_id=mutation_event.mutation_event_id");
            rs = pstmt.executeQuery();
            while (rs.next()) {
                ExtendedMutation mutation = extractMutation(rs);
                mutationList.add(mutation);
            }
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoMutation.class, con, pstmt, rs);
        }
        return mutationList;
    }

    /**
     * Similar to getAllMutations(), but filtered by a passed geneticProfileId
     * @param geneticProfileId
     * @return
     * @throws DaoException
     *
     * @deprecated  We believe that this method is no longer called by any part of the codebase, and it will soon be deleted.
     */
    @Deprecated
    public static ArrayList<ExtendedMutation> getAllMutations (int geneticProfileId) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        ArrayList <ExtendedMutation> mutationList = new ArrayList <ExtendedMutation>();
        try {
            con = JdbcUtil.getDbConnection(DaoMutation.class);
            pstmt = con.prepareStatement(
                    "SELECT * FROM mutation " +
                    "INNER JOIN mutation_event ON mutation.mutation_event_id=mutation_event.mutation_event_id " +
                    "WHERE mutation.genetic_profile_id = ?");
            pstmt.setInt(1, geneticProfileId);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                ExtendedMutation mutation = extractMutation(rs);
                mutationList.add(mutation);
            }
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoMutation.class, con, pstmt, rs);
        }
        return mutationList;
    }

    public static Set<ExtendedMutation.MutationEvent> getAllMutationEvents() throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        Set<ExtendedMutation.MutationEvent> events = new HashSet<ExtendedMutation.MutationEvent>();
        try {
            con = JdbcUtil.getDbConnection(DaoMutation.class);
            pstmt = con.prepareStatement("SELECT * FROM mutation_event");
            rs = pstmt.executeQuery();
            while (rs.next()) {
                ExtendedMutation.MutationEvent event = extractMutationEvent(rs);
                events.add(event);
            }
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoMutation.class, con, pstmt, rs);
        }
        return events;
    }

    public static long getLargestMutationEventId() throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoMutation.class);
            pstmt = con.prepareStatement("SELECT max(`mutation_event_id`) FROM `mutation_event`");
            rs = pstmt.executeQuery();
            return rs.next() ? rs.getLong(1) : 0;
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoMutation.class, con, pstmt, rs);
        }
    }

    private static ExtendedMutation extractMutation(ResultSet rs) throws SQLException, DaoException {
        try {
            ExtendedMutation mutation = new ExtendedMutation(extractMutationEvent(rs));
            mutation.setGeneticProfileId(rs.getInt("genetic_profile_id"));
            mutation.setSampleId(rs.getInt("sample_id"));
            mutation.setSequencingCenter(rs.getString("center"));
            mutation.setSequencer(rs.getString("sequencer"));
            mutation.setMutationStatus(rs.getString("mutation_status"));
            mutation.setValidationStatus(rs.getString("validation_status"));
            mutation.setTumorSeqAllele1(rs.getString("tumor_seq_allele1"));
            mutation.setTumorSeqAllele2(rs.getString("tumor_seq_allele2"));
            mutation.setMatchedNormSampleBarcode(rs.getString("matched_norm_sample_barcode"));
            mutation.setMatchNormSeqAllele1(rs.getString("match_norm_seq_allele1"));
            mutation.setMatchNormSeqAllele2(rs.getString("match_norm_seq_allele2"));
            mutation.setTumorValidationAllele1(rs.getString("tumor_validation_allele1"));
            mutation.setTumorValidationAllele2(rs.getString("tumor_validation_allele2"));
            mutation.setMatchNormValidationAllele1(rs.getString("match_norm_validation_allele1"));
            mutation.setMatchNormValidationAllele2(rs.getString("match_norm_validation_allele2"));
            mutation.setVerificationStatus(rs.getString("verification_status"));
            mutation.setSequencingPhase(rs.getString("sequencing_phase"));
            mutation.setSequenceSource(rs.getString("sequence_source"));
            mutation.setValidationMethod(rs.getString("validation_method"));
            mutation.setScore(rs.getString("score"));
            mutation.setBamFile(rs.getString("bam_file"));
            // the following checks/set of null is to maintain
            // behavior with MutationRepositoryLegacy (mybatis) behavior
            // whose use is being retired in this PR
            mutation.setTumorAltCount(rs.getInt("tumor_alt_count"));
            if (rs.wasNull()) mutation.setTumorAltCount(null);
            mutation.setTumorRefCount(rs.getInt("tumor_ref_count"));
            if (rs.wasNull()) mutation.setTumorRefCount(null);
            mutation.setNormalAltCount(rs.getInt("normal_alt_count"));
            if (rs.wasNull()) mutation.setNormalAltCount(null);
            mutation.setNormalRefCount(rs.getInt("normal_ref_count"));
            if (rs.wasNull()) mutation.setNormalRefCount(null);
            mutation.setAnnotationJson(rs.getString("annotation_json"));
            return mutation;
        }
        catch(NullPointerException e) {
            throw new DaoException(e);
        }
    }

    private static ExtendedMutation.MutationEvent extractMutationEvent(ResultSet rs) throws SQLException, DaoException {
        ExtendedMutation.MutationEvent event = new ExtendedMutation.MutationEvent();
        event.setMutationEventId(rs.getLong("mutation_event_id"));
        long entrezId = rs.getLong("entrez_gene_id");
        DaoGeneOptimized aDaoGene = DaoGeneOptimized.getInstance();
        CanonicalGene gene = aDaoGene.getGene(entrezId);
        event.setGene(gene);
        event.setChr(rs.getString("chr"));
        event.setStartPosition(rs.getLong("start_position"));
        event.setEndPosition(rs.getLong("end_position"));
        event.setProteinChange(rs.getString("protein_change"));
        event.setMutationType(rs.getString("mutation_type"));
        event.setNcbiBuild(rs.getString("ncbi_build"));
        event.setStrand(rs.getString("strand"));
        event.setVariantType(rs.getString("variant_type"));
        event.setDbSnpRs(rs.getString("db_snp_rs"));
        event.setDbSnpValStatus(rs.getString("db_snp_val_status"));
        event.setReferenceAllele(rs.getString("reference_allele"));
        event.setRefseqMrnaId(rs.getString("refseq_mrna_id"));
        event.setCodonChange(rs.getString("codon_change"));
        event.setUniprotAccession(rs.getString("uniprot_accession"));
        event.setProteinPosStart(rs.getInt("protein_pos_start"));
        event.setProteinPosEnd(rs.getInt("protein_pos_end"));
        event.setCanonicalTranscript(rs.getBoolean("canonical_transcript"));
        event.setTumorSeqAllele(rs.getString("tumor_seq_allele"));
        event.setKeyword(rs.getString("keyword"));
        return event;
    }

    /**
     * @deprecated  We believe that this method is no longer called by any part of the codebase, and it will soon be deleted.
     */
    @Deprecated
    public static int getCount() throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoMutation.class);
            pstmt = con.prepareStatement("SELECT count(*) FROM mutation");
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoMutation.class, con, pstmt, rs);
        }
    }

    /**
     * Get significantly mutated genes
     * @param profileId
     * @param entrezGeneIds
     * @param thresholdRecurrence
     * @param thresholdNumGenes
     * @param selectedCaseIds
     * @return
     * @throws DaoException
     * @deprecated  We believe that this method is no longer called by any part of the codebase, and it will soon be deleted.
     */
    @Deprecated
    public static Map<Long, Map<String, String>> getSMGs(int profileId, Collection<Long> entrezGeneIds,
            int thresholdRecurrence, int thresholdNumGenes,
            Collection<Integer> selectedCaseIds) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoMutation.class);
            pstmt = con.prepareStatement("SET SESSION group_concat_max_len = 1000000");
            rs = pstmt.executeQuery();
            final Connection queryCon = con;
            return ClickHouseBulkUploader.upload(entrezGeneIds, geneTable ->
                ClickHouseBulkUploader.upload(selectedCaseIds, caseTable -> {
                    String sql = "SELECT mutation.entrez_gene_id, group_concat(mutation.sample_id), count(*), count(*)/`length` AS count_per_nt" +
                            " FROM mutation, gene" +
                            " WHERE mutation.entrez_gene_id=gene.entrez_gene_id" +
                            " AND genetic_profile_id=" + profileId +
                            (geneTable == null ? "" : " AND mutation.entrez_gene_id IN (SELECT id FROM " + geneTable + ")") +
                            (caseTable == null ? "" : " AND mutation.sample_id IN (SELECT id FROM " + caseTable + ")") +
                            " GROUP BY mutation.entrez_gene_id" +
                            (thresholdRecurrence > 0 ? " HAVING COUNT(*)>=" + thresholdRecurrence : "") +
                            " ORDER BY count_per_nt DESC" +
                            (thresholdNumGenes > 0 ? " LIMIT 0," + thresholdNumGenes : "");
                    try (PreparedStatement stmt = queryCon.prepareStatement(sql);
                         ResultSet resultSet = stmt.executeQuery()) {
                        Map<Long, Map<String, String>> map = new HashMap<>();
                        while (resultSet.next()) {
                            Map<String, String> value = new HashMap<>();
                            value.put("caseIds", resultSet.getString(2));
                            value.put("count", resultSet.getString(3));
                            map.put(resultSet.getLong(1), value);
                        }
                        return map;
                    }
                }));
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoMutation.class, con, pstmt, rs);
        }
    }

    /**
     * return the number of all mutations for a profile
     * @param profileId
     * @return Map &lt; case id, mutation count &gt;
     * @throws DaoException
     *
     * @deprecated  We believe that this method is no longer called by any part of the codebase, and it will soon be deleted.
     */
    @Deprecated
    public static int countMutationEvents(int profileId) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoMutation.class);
            String sql = "SELECT count(DISTINCT `sample_id`, `mutation_event_id`) FROM mutation" +
                    " WHERE `genetic_profile_id`=" + profileId;
            pstmt = con.prepareStatement(sql);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoMutation.class, con, pstmt, rs);
        }
    }

    /**
     * get events for each sample
     * @return Map &lt; sample id, list of event ids &gt;
     * @throws DaoException
     *
     * @deprecated  We believe that this method is no longer called by any part of the codebase, and it will soon be deleted.
     */
    @Deprecated
    public static Map<Integer, Set<Long>> getSamplesWithMutations(Collection<Long> eventIds) throws DaoException {
        return getSamplesWithMutations(StringUtils.join(eventIds, ","));
    }

    /**
     * get events for each sample
     * @param concatEventIds event ids concatenated by comma (,)
     * @return Map &lt; sample id, list of event ids &gt;
     * @throws DaoException
     *
     * @deprecated  We believe that this method is no longer called by any part of the codebase, and it will soon be deleted.
     */
    @Deprecated
    public static Map<Integer, Set<Long>> getSamplesWithMutations(String concatEventIds) throws DaoException {
        if (concatEventIds.isEmpty()) {
            return Collections.emptyMap();
        }
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoMutation.class);
            String sql = "SELECT `sample_id`, `mutation_event_id` FROM mutation" +
                    " WHERE `mutation_event_id` IN (" +
                    concatEventIds + ")";
            pstmt = con.prepareStatement(sql);
            Map<Integer, Set<Long>> map = new HashMap<Integer, Set<Long>> ();
            rs = pstmt.executeQuery();
            while (rs.next()) {
                int sampleId = rs.getInt("sample_id");
                long eventId = rs.getLong("mutation_event_id");
                Set<Long> events = map.get(sampleId);
                if (events == null) {
                    events = new HashSet<Long>();
                    map.put(sampleId, events);
                }
                events.add(eventId);
            }
            return map;
        } catch (NullPointerException e) {
            throw new DaoException(e);
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoMutation.class, con, pstmt, rs);
        }
    }

    /**
     * @return Map &lt; sample, list of event ids &gt;
     * @throws DaoException
     * @deprecated  We believe that this method is no longer called by any part of the codebase, and it will soon be deleted.
     */
    @Deprecated
    public static Map<Sample, Set<Long>> getSimilarSamplesWithMutationsByKeywords(
            Collection<Long> eventIds) throws DaoException {
        return getSimilarSamplesWithMutationsByKeywords(StringUtils.join(eventIds, ","));
    }

    /**
     * @param concatEventIds event ids concatenated by comma (,)
     * @return Map &lt; sample, list of event ids &gt;
     * @throws DaoException
     */
    public static Map<Sample, Set<Long>> getSimilarSamplesWithMutationsByKeywords(
            String concatEventIds) throws DaoException {
        if (concatEventIds.isEmpty()) {
            return Collections.emptyMap();
        }
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoMutation.class);
            String sql = "SELECT `sample_id`, `genetic_profile_id`, me1.`mutation_event_id`" +
                    " FROM mutation cme, mutation_event me1, mutation_event me2" +
                    " WHERE me1.`mutation_event_id` IN ("+ concatEventIds + ")" +
                    " AND me1.`keyword`=me2.`keyword`" +
                    " AND cme.`mutation_event_id`=me2.`mutation_event_id`";
            pstmt = con.prepareStatement(sql);
            Map<Sample, Set<Long>> map = new HashMap<Sample, Set<Long>> ();
            rs = pstmt.executeQuery();
            while (rs.next()) {
                Sample sample = DaoSample.getSampleById(rs.getInt("sample_id"));
                long eventId = rs.getLong("mutation_event_id");
                Set<Long> events = map.get(sample);
                if (events == null) {
                    events = new HashSet<Long>();
                    map.put(sample, events);
                }
                events.add(eventId);
            }
            return map;
        } catch (NullPointerException e) {
            throw new DaoException(e);
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoMutation.class, con, pstmt, rs);
        }
    }

    /**
     * @param entrezGeneIds event ids concatenated by comma (,)
     * @return Map &lt; sample, list of event ids &gt;
     * @throws DaoException
     *
     * @deprecated  We believe that this method is no longer called by any part of the codebase, and it will soon be deleted.
     */
    @Deprecated
    public static Map<Sample, Set<Long>> getSimilarSamplesWithMutatedGenes(
            Collection<Long> entrezGeneIds) throws DaoException {
        if (entrezGeneIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return ClickHouseBulkUploader.upload(entrezGeneIds, stagingTable -> {
            Map<Sample, Set<Long>> map = new HashMap<>();
            Connection con = null;
            try {
                con = JdbcUtil.getDbConnection(DaoMutation.class);
                try (PreparedStatement pstmt = con.prepareStatement(
                        "SELECT `sample_id`, `genetic_profile_id`, `entrez_gene_id`" +
                        " FROM mutation" +
                        " WHERE `entrez_gene_id` IN (SELECT id FROM " + stagingTable + ")");
                     ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        Sample sample = DaoSample.getSampleById(rs.getInt("sample_id"));
                        long entrez = rs.getLong("entrez_gene_id");
                        Set<Long> genes = map.get(sample);
                        if (genes == null) {
                            genes = new HashSet<>();
                            map.put(sample, genes);
                        }
                        genes.add(entrez);
                    }
                }
                return map;
            } finally {
                JdbcUtil.closeAll(DaoMutation.class, con, null, null);
            }
        });
    }

    /**
     * @deprecated  We believe that this method is no longer called by any part of the codebase, and it will soon be deleted.
     */
    @Deprecated
    public static Map<Long, Integer> countSamplesWithMutationEvents(Collection<Long> eventIds, int profileId) throws DaoException {
        return countSamplesWithMutationEvents(StringUtils.join(eventIds, ","), profileId);
    }

    /**
     * return the number of samples for each mutation event
     * @param concatEventIds
     * @param profileId
     * @return Map &lt; event id, sampleCount &gt;
     * @throws DaoException
     *
     * @deprecated  We believe that this method is no longer called by any part of the codebase, and it will soon be deleted.
     */
    @Deprecated
    public static Map<Long, Integer> countSamplesWithMutationEvents(String concatEventIds, int profileId) throws DaoException {
        if (concatEventIds.isEmpty()) {
            return Collections.emptyMap();
        }
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoMutation.class);
            String sql = "SELECT `mutation_event_id`, count(DISTINCT `sample_id`) FROM mutation" +
                    " WHERE `genetic_profile_id`=" + profileId +
                    " AND `mutation_event_id` IN (" +
                    concatEventIds +
                    ") GROUP BY `mutation_event_id`";
            pstmt = con.prepareStatement(sql);
            Map<Long, Integer> map = new HashMap<Long, Integer>();
            rs = pstmt.executeQuery();
            while (rs.next()) {
                map.put(rs.getLong(1), rs.getInt(2));
            }
            return map;
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoMutation.class, con, pstmt, rs);
        }
    }

    /**
     * @deprecated  We believe that this method is no longer called by any part of the codebase, and it will soon be deleted.
     */
    @Deprecated
    public static Map<Long, Integer> countSamplesWithMutatedGenes(Collection<Long> entrezGeneIds, int profileId) throws DaoException {
        return countSamplesWithMutatedGenes(StringUtils.join(entrezGeneIds, ","), profileId);
    }

    /**
     * return the number of samples for each mutated genes
     * @param concatEntrezGeneIds
     * @param profileId
     * @return Map &lt; entrez, sampleCount &gt;
     * @throws DaoException
     * @deprecated  We believe that this method is no longer called by any part of the codebase, and it will soon be deleted.
     */
    @Deprecated
    public static Map<Long, Integer> countSamplesWithMutatedGenes(String concatEntrezGeneIds, int profileId) throws DaoException {
        if (concatEntrezGeneIds.isEmpty()) {
            return Collections.emptyMap();
        }
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoMutation.class);
            String sql = "SELECT entrez_gene_id, count(DISTINCT sample_id)" +
                    " FROM mutation" +
                    " WHERE genetic_profile_id=" + profileId +
                    " AND entrez_gene_id IN (" +
                    concatEntrezGeneIds +
                    ") GROUP BY `entrez_gene_id`";
            pstmt = con.prepareStatement(sql);
            Map<Long, Integer> map = new HashMap<Long, Integer>();
            rs = pstmt.executeQuery();
            while (rs.next()) {
                map.put(rs.getLong(1), rs.getInt(2));
            }
            return map;
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoMutation.class, con, pstmt, rs);
        }
    }

    /**
     * @deprecated  We believe that this method is no longer called by any part of the codebase, and it will soon be deleted.
     */
    @Deprecated
    public static Map<String, Integer> countSamplesWithKeywords(Collection<String> keywords, int profileId) throws DaoException {
        if (keywords.isEmpty()) {
            return Collections.emptyMap();
        }
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoMutation.class);
            String sql = "SELECT keyword, count(DISTINCT sample_id)" +
                    " FROM mutation, mutation_event" +
                    " WHERE genetic_profile_id=" + profileId +
                    " AND mutation.mutation_event_id=mutation_event.mutation_event_id" +
                    " AND keyword IN ('" +
                    StringUtils.join(keywords,"','") +
                    "') GROUP BY `keyword`";
            pstmt = con.prepareStatement(sql);
            Map<String, Integer> map = new HashMap<String, Integer>();
            rs = pstmt.executeQuery();
            while (rs.next()) {
                map.put(rs.getString(1), rs.getInt(2));
            }
            return map;
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoMutation.class, con, pstmt, rs);
        }
    }

    /**
     *
     * Counts all the samples in each cancer study in the collection of geneticProfileIds by mutation keyword
     *
     * @param keywords
     * @param internalProfileIds
     * @return Collection of Maps {"keyword" , "hugo" , "cancer_study" , "count"} where cancer_study == cancerStudy.getName();
     * @throws DaoException
     * @author Gideon Dresdner <dresdnerg@cbio.mskcc.org>
     */
    public static Collection<Map<String, Object>> countSamplesWithKeywords(Collection<String> keywords, Collection<Integer> internalProfileIds) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoMutation.class);
            String sql = "SELECT keyword, genetic_profile_id, mutation.entrez_gene_id, count(DISTINCT sample_id) FROM mutation, mutation_event " +
                    "WHERE genetic_profile_id IN (" + StringUtils.join(internalProfileIds, ",") + ") " +
                    "AND mutation.mutation_event_id=mutation_event.mutation_event_id " +
                    "AND keyword IN ('" + StringUtils.join(keywords, "','") + "') " +
                    "GROUP BY keyword, genetic_profile_id, mutation.entrez_gene_id";
            pstmt = con.prepareStatement(sql);
            rs = pstmt.executeQuery();
            Collection<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
            while (rs.next()) {
                Map<String, Object> d = new HashMap<String, Object>();
                String keyword = rs.getString(1);
                Integer geneticProfileId = rs.getInt(2);
                Long entrez = rs.getLong(3);
                Integer count = rs.getInt(4);
                // this is computing a join and in not optimal
                GeneticProfile geneticProfile = DaoGeneticProfile.getGeneticProfileById(geneticProfileId);
                Integer cancerStudyId = geneticProfile.getCancerStudyId();
                CancerStudy cancerStudy = DaoCancerStudy.getCancerStudyByInternalId(cancerStudyId);
                String name = cancerStudy.getName();
                String cancerType = cancerStudy.getTypeOfCancerId();
                CanonicalGene gene = DaoGeneOptimized.getInstance().getGene(entrez);
                String hugo = gene.getHugoGeneSymbolAllCaps();
                d.put("keyword", keyword);
                d.put("hugo", hugo);
                d.put("cancer_study", name);
                d.put("cancer_type", cancerType);
                d.put("count", count);
                data.add(d);
            }
            return data;
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoMutation.class, con, pstmt, rs);
        }
    }

    /**
     *
     * Counts up all the samples that have any mutation in a gene by genomic profile (ids)
     *
     * @param hugos
     * @param internalProfileIds
     * @return Collection of Maps {"hugo" , "cancer_study" , "count"} where cancer_study == cancerStudy.getName();
     * and gene is the hugo gene symbol.
     *
     * @throws DaoException
     * @author Gideon Dresdner <dresdnerg@cbio.mskcc.org>
     */
    public static Collection<Map<String, Object>> countSamplesWithGenes(Collection<String> hugos, Collection<Integer> internalProfileIds) throws DaoException {
        // convert hugos to entrezs
        // and simultaneously construct a map to turn them back into hugo gene symbols later
        List<Long> entrezs = new ArrayList<Long>();
        Map<Long, CanonicalGene> entrez2CanonicalGene = new HashMap<Long, CanonicalGene>();
        DaoGeneOptimized daoGeneOptimized = DaoGeneOptimized.getInstance();
        for (String hugo : hugos) {
            CanonicalGene canonicalGene = daoGeneOptimized.getGene(hugo);
            Long entrez = canonicalGene.getEntrezGeneId();
            entrezs.add(entrez);
            entrez2CanonicalGene.put(entrez, canonicalGene);
        }
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoMutation.class);
            String sql = "select mutation.entrez_gene_id, mutation.genetic_profile_id, count(distinct sample_id) from mutation, mutation_event\n" +
                    "where genetic_profile_id in (" + StringUtils.join(internalProfileIds, ",") + ")\n" +
                    "and mutation.entrez_gene_id in (" + StringUtils.join(entrezs, ",") + ")\n" +
                    "and mutation.mutation_event_id=mutation_event.mutation_event_id\n" +
                    "group by entrez_gene_id, genetic_profile_id";
            pstmt = con.prepareStatement(sql);
            rs = pstmt.executeQuery();
            Collection<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
            while (rs.next()) {
                Map<String, Object> d = new HashMap<String, Object>();
                Long entrez = rs.getLong(1);
                Integer geneticProfileId = rs.getInt(2);
                Integer count = rs.getInt(3);
                // can you get a cancerStudy's name?
                // this is computing a join and in not optimal
                GeneticProfile geneticProfile = DaoGeneticProfile.getGeneticProfileById(geneticProfileId);
                Integer cancerStudyId = geneticProfile.getCancerStudyId();
                CancerStudy cancerStudy = DaoCancerStudy.getCancerStudyByInternalId(cancerStudyId);
                String name = cancerStudy.getName();
                String cancerType = cancerStudy.getTypeOfCancerId();
                CanonicalGene canonicalGene = entrez2CanonicalGene.get(entrez);
                String hugo = canonicalGene.getHugoGeneSymbolAllCaps();
                d.put("hugo", hugo);
                d.put("cancer_study", name);
                d.put("cancer_type", cancerType);
                d.put("count", count);
                data.add(d);
            }
            return data;
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoMutation.class, con, pstmt, rs);
        }
    }

    public static Collection<Map<String, Object>> countSamplesWithProteinChanges(
            Collection<String> proteinChanges, Collection<Integer> internalProfileIds) throws DaoException
    {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoMutation.class);
            String sql = "SELECT protein_change, genetic_profile_id, mutation.entrez_gene_id, count(DISTINCT sample_id) FROM mutation, mutation_event " +
                         "WHERE genetic_profile_id IN (" + StringUtils.join(internalProfileIds, ",") + ") " +
                         "AND mutation.mutation_event_id=mutation_event.mutation_event_id " +
                         "AND protein_change IN ('" + StringUtils.join(proteinChanges, "','") + "') " +
                         "GROUP BY protein_change, genetic_profile_id, mutation.entrez_gene_id";
            pstmt = con.prepareStatement(sql);
            rs = pstmt.executeQuery();
            Collection<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
            while (rs.next()) {
                Map<String, Object> d = new HashMap<String, Object>();
                String proteinChange = rs.getString(1);
                Integer geneticProfileId = rs.getInt(2);
                Long entrez = rs.getLong(3);
                Integer count = rs.getInt(4);
                // can you get a cancerStudy's name?
                // this is computing a join and in not optimal
                GeneticProfile geneticProfile = DaoGeneticProfile.getGeneticProfileById(geneticProfileId);
                Integer cancerStudyId = geneticProfile.getCancerStudyId();
                CancerStudy cancerStudy = DaoCancerStudy.getCancerStudyByInternalId(cancerStudyId);
                String name = cancerStudy.getName();
                String cancerType = cancerStudy.getTypeOfCancerId();
                CanonicalGene gene = DaoGeneOptimized.getInstance().getGene(entrez);
                String hugo = gene.getHugoGeneSymbolAllCaps();
                d.put("protein_change", proteinChange);
                d.put("hugo", hugo);
                d.put("cancer_study", name);
                d.put("cancer_type", cancerType);
                d.put("count", count);
                data.add(d);
            }
            return data;
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoMutation.class, con, pstmt, rs);
        }
    }

    public static Collection<Map<String, Object>> countSamplesWithProteinPosStarts(
            Collection<String> proteinPosStarts, Collection<Integer> internalProfileIds) throws DaoException
    {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        //performance fix: mutation table contains geneId; by filtering on a geneId set before table join, the temporary table needed is smaller.
        //a geneticProfileId set filter alone can in some cases let almost all mutations into the temporary table
        HashSet<String> geneIdSet = new HashSet<String>();
        if (proteinPosStarts != null) {
            Pattern geneIdPattern = Pattern.compile("\\(\\s*(\\d+)\\s*,");
            for (String proteinPos : proteinPosStarts) {
                Matcher geneIdMatcher = geneIdPattern.matcher(proteinPos);
                if (geneIdMatcher.find()) {
                    geneIdSet.add(geneIdMatcher.group(1));
                }
            }
        }
        if (geneIdSet.size() == 0 || internalProfileIds.size() == 0) return new ArrayList<Map<String, Object>>(); //empty IN() clause would be a SQL error below
        try {
            con = JdbcUtil.getDbConnection(DaoMutation.class);
            String sql = "SELECT protein_pos_start, genetic_profile_id, mutation.entrez_gene_id, count(DISTINCT sample_id) " +
                    "FROM mutation INNER JOIN mutation_event ON mutation.mutation_event_id=mutation_event.mutation_event_id " +
                    "WHERE mutation.entrez_gene_id IN (" + StringUtils.join(geneIdSet, ",") + ") " +
                    "AND genetic_profile_id IN (" + StringUtils.join(internalProfileIds, ",") + ") " +
                    "AND (mutation.entrez_gene_id, protein_pos_start) IN (" + StringUtils.join(proteinPosStarts, ",") + ") " +
                    "GROUP BY protein_pos_start, genetic_profile_id, mutation.entrez_gene_id";
            pstmt = con.prepareStatement(sql);
            rs = pstmt.executeQuery();
            Collection<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
            while (rs.next()) {
                Map<String, Object> d = new HashMap<String, Object>();
                String proteinPosStart = rs.getString(1);
                Integer geneticProfileId = rs.getInt(2);
                Long entrez = rs.getLong(3);
                Integer count = rs.getInt(4);
                // can you get a cancerStudy's name?
                // this is computing a join and in not optimal
                GeneticProfile geneticProfile = DaoGeneticProfile.getGeneticProfileById(geneticProfileId);
                Integer cancerStudyId = geneticProfile.getCancerStudyId();
                CancerStudy cancerStudy = DaoCancerStudy.getCancerStudyByInternalId(cancerStudyId);
                String name = cancerStudy.getName();
                String cancerType = cancerStudy.getTypeOfCancerId();
                CanonicalGene gene = DaoGeneOptimized.getInstance().getGene(entrez);
                String hugo = gene.getHugoGeneSymbolAllCaps();
                d.put("protein_pos_start", proteinPosStart);
                d.put("protein_start_with_hugo", hugo+"_"+proteinPosStart);
                d.put("hugo", hugo);
                d.put("cancer_study", name);
                d.put("cancer_type", cancerType);
                d.put("count", count);
                data.add(d);
            }
            return data;
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoMutation.class, con, pstmt, rs);
        }
    }

    /**
     * @deprecated  We believe that this method is no longer called by any part of the codebase, and it will soon be deleted.
     */
    @Deprecated
    public static Set<Long> getGenesOfMutations(
            Collection<Long> eventIds, int profileId) throws DaoException {
        return getGenesOfMutations(StringUtils.join(eventIds, ","), profileId);
    }

    /**
     * return entrez gene ids of the mutations specified by their mutaiton event ids.
     * @param concatEventIds
     * @param profileId
     * @return
     * @throws DaoException
     * @deprecated  We believe that this method is no longer called by any part of the codebase, and it will soon be deleted.
     */
    @Deprecated
    public static Set<Long> getGenesOfMutations(String concatEventIds, int profileId)
            throws DaoException {
        if (concatEventIds.isEmpty()) {
            return Collections.emptySet();
        }
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoMutation.class);
            String sql = "SELECT DISTINCT entrez_gene_id FROM mutation_event " +
                    "WHERE mutation_event_id in (" + concatEventIds + ")";
            pstmt = con.prepareStatement(sql);
            Set<Long> set = new HashSet<Long>();
            rs = pstmt.executeQuery();
            while (rs.next()) {
                set.add(rs.getLong(1));
            }
            return set;
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoMutation.class, con, pstmt, rs);
        }
    }

    /**
     * return keywords of the mutations specified by their mutaiton event ids.
     * @param concatEventIds
     * @param profileId
     * @return
     * @throws DaoException
     * @deprecated  We believe that this method is no longer called by any part of the codebase, and it will soon be deleted.
     */
    @Deprecated
    public static Set<String> getKeywordsOfMutations(String concatEventIds, int profileId)
            throws DaoException {
        if (concatEventIds.isEmpty()) {
            return Collections.emptySet();
        }
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoMutation.class);
            String sql = "SELECT DISTINCT keyword FROM mutation_event " +
                    "WHERE mutation_event_id in (" + concatEventIds + ")";
            pstmt = con.prepareStatement(sql);
            Set<String> set = new HashSet<String>();
            rs = pstmt.executeQuery();
            while (rs.next()) {
                set.add(rs.getString(1));
            }
            return set;
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoMutation.class, con, pstmt, rs);
        }
    }

    protected static String boolToStr(boolean value)
    {
        return value ? "1" : "0";
    }

    public static void deleteAllRecordsInGeneticProfileForSample(long geneticProfileId, long internalSampleId) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoMutation.class);
            pstmt = con.prepareStatement(DELETE_ALTERATION_DRIVER_ANNOTATION);
            pstmt.setLong(1, geneticProfileId);
            pstmt.setLong(2, internalSampleId);
            pstmt.executeUpdate();

            pstmt = con.prepareStatement(DELETE_MUTATION);
            pstmt.setLong(1, geneticProfileId);
            pstmt.setLong(2, internalSampleId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoMutation.class, con, pstmt, rs);
        }
    }

    public static void deleteAllRecords() throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoMutation.class);
            pstmt = con.prepareStatement("TRUNCATE TABLE mutation");
            pstmt.executeUpdate();
            pstmt = con.prepareStatement("TRUNCATE TABLE mutation_event");
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoMutation.class, con, pstmt, rs);
        }
    }

    /**
     * Gets all tiers in alphabetical order.
     *
     * @param
     * @return Ordered list of tiers.
     * @throws DaoException Database Error.
     */
    public static List<String> getTiers(String _cancerStudyStableIds) throws DaoException {
        String[] cancerStudyStableIds = _cancerStudyStableIds.split(",");
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<String> tiers = new ArrayList<String>();
        ArrayList<GeneticProfile> geneticProfiles = new ArrayList<>();
        for (String cancerStudyStableId: cancerStudyStableIds) {
            geneticProfiles.addAll(DaoGeneticProfile.getAllGeneticProfiles(
                DaoCancerStudy.getCancerStudyByStableId(cancerStudyStableId).getInternalId()
            ));
        }
        for (GeneticProfile geneticProfile : geneticProfiles) {
            if (geneticProfile.getGeneticAlterationType().equals(GeneticAlterationType.MUTATION_EXTENDED)) {
                try {
                    con = JdbcUtil.getDbConnection(DaoMutation.class);
                    pstmt = con.prepareStatement(
                            "SELECT DISTINCT driver_tiers_filter FROM alteration_driver_annotation "
                            + "WHERE driver_tiers_filter is not NULL AND driver_tiers_filter <> '' AND genetic_profile_id=? "
                            + "ORDER BY driver_tiers_filter");
                    pstmt.setLong(1, geneticProfile.getGeneticProfileId());
                    rs = pstmt.executeQuery();
                    while (rs.next()) {
                        tiers.add(rs.getString("driver_tiers_filter"));
                    }
                } catch (SQLException e) {
                    throw new DaoException(e);
                } finally {
                    JdbcUtil.closeAll(DaoMutation.class, con, pstmt, rs);
                }
            }
        }

        return tiers;
    }

    /**
     * Returns the number of tiers in the cancer study..
     *
     * @param
     * @return Ordered list of tiers.
     * @throws DaoException Database Error.
     */
    public static int numTiers(String _cancerStudyStableIds) throws DaoException {
        List<String> tiers = getTiers(_cancerStudyStableIds);
        return tiers.size();
    }

    /**
     * Returns true if there are "Putative_Driver" or "Putative_Passenger" values in the
     * binary annotation column. Otherwise, it returns false.
     *
     * @param
     * @return Ordered list of tiers.
     * @throws DaoException Database Error.
     */
    public static boolean hasDriverAnnotations(String _cancerStudyStableIds) throws DaoException {
        String[] cancerStudyStableIds = _cancerStudyStableIds.split(",");
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<String> driverValues = new ArrayList<String>();
        ArrayList<GeneticProfile> geneticProfiles = new ArrayList<>();
        for (String cancerStudyStableId: cancerStudyStableIds) {
            geneticProfiles.addAll(
                DaoGeneticProfile.getAllGeneticProfiles(DaoCancerStudy.getCancerStudyByStableId(cancerStudyStableId).getInternalId())
            );
        }
        for (GeneticProfile geneticProfile : geneticProfiles) {
            if (geneticProfile.getGeneticAlterationType().equals(GeneticAlterationType.MUTATION_EXTENDED)) {
                try {
                    con = JdbcUtil.getDbConnection(DaoMutation.class);
                    pstmt = con.prepareStatement(
                            "SELECT DISTINCT driver_filter FROM alteration_driver_annotation "
                            + "WHERE driver_filter is not NULL AND driver_filter <> '' AND genetic_profile_id=? ");
                    pstmt.setLong(1, geneticProfile.getGeneticProfileId());
                    rs = pstmt.executeQuery();
                    while (rs.next()) {
                        driverValues.add(rs.getString("driver_filter"));
                    }
                } catch (SQLException e) {
                    throw new DaoException(e);
                } finally {
                    JdbcUtil.closeAll(DaoMutation.class, con, pstmt, rs);
                }
            }
        }
        if (driverValues.size() > 0) {
            return true;
        }
        return false;
    }

    /**
     * Returns true if there are duplicates in `mutation_event`.
     * Compares count(*) of records in `mutation_event` against the count(distinct ...)
     * records based on the fields specified below.
     *
     * If the counts do not match then SQL statement returns 0 (false), otherwise returns 1 (true).
     * @return
     * @throws DaoException
     */
    public static boolean hasDuplicateMutationEvents() throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        Integer sqlQueryResult = -1; // default = -1 to indicate mismatched counts from db table
        try {
            con = JdbcUtil.getDbConnection(DaoMutation.class);
            pstmt = con.prepareStatement(
                    "SELECT "
                    + "(SELECT count(*) FROM mutation_event) = "
                    + "(SELECT count(DISTINCT entrez_gene_id, chr, start_position, end_position, tumor_seq_allele, protein_change, mutation_type) FROM mutation_event)");
            rs = pstmt.executeQuery();
            if (rs.next()) {
                sqlQueryResult = rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoMutation.class, con, pstmt, rs);
        }
        if (sqlQueryResult == -1) {
            throw new DaoException("Error occurred while executing SQL query to detect duplicate records in `mutation_event`");
        }
        return !(sqlQueryResult > 0);
    }
}
