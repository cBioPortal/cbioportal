/*
 * Copyright (c) 2017 - 2022 The Hyve B.V.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.mskcc.cbio.portal.dao;

import java.sql.*;
import java.util.*;
import org.mskcc.cbio.portal.model.StructuralVariant;

public class DaoStructuralVariant {

    private DaoStructuralVariant() {
    }
    /**
     * Adds a new Structural variant record to the database.
     * @param structuralVariant
     * @return number of records successfully added
     * @throws DaoException
     */

    public static void addStructuralVariantToBulkLoader(StructuralVariant structuralVariant) throws DaoException {
        ClickHouseBulkLoader bl =  ClickHouseBulkLoader.getClickHouseBulkLoader("structural_variant");
        String[] fieldNames = new String[]{
            "internal_id",
            "genetic_profile_id",
            "sample_id",
            "site1_entrez_gene_id",
            "site1_ensembl_transcript_id",
            "site1_chromosome",
            "site1_position",
            "site1_contig",
            "site1_region",
            "site1_region_number",
            "site1_description",
            "site2_entrez_gene_id",
            "site2_ensembl_transcript_id",
            "site2_chromosome",
            "site2_position",
            "site2_contig",
            "site2_region",
            "site2_region_number",
            "site2_description",
            "site2_effect_on_frame",
            "ncbi_build",
            "dna_support",
            "rna_support",
            "normal_read_count",
            "tumor_read_count",
            "normal_variant_count",
            "tumor_variant_count",
            "normal_paired_end_read_count",
            "tumor_paired_end_read_count",
            "normal_split_read_count",
            "tumor_split_read_count",
            "annotation",
            "breakpoint_type",
            "connection_type",
            "event_info",
            "class",
            "length",
            "comments",
            "sv_status",
            "annotation_json"
        };
        bl.setFieldNames(fieldNames);

        // write to the temp file maintained by the ClickHouseBulkLoader
        bl.insertRecord(
            Long.toString(structuralVariant.getInternalId()),
            Integer.toString(structuralVariant.getGeneticProfileId()),
            Integer.toString(structuralVariant.getSampleIdInternal()),
            structuralVariant.getSite1EntrezGeneId() == null ? null : Long.toString(structuralVariant.getSite1EntrezGeneId()),
            structuralVariant.getSite1EnsemblTranscriptId(),
            structuralVariant.getSite1Chromosome(),
            Integer.toString(structuralVariant.getSite1Position()),
            structuralVariant.getSite1Contig(),
            structuralVariant.getSite1Region(),
            Integer.toString(structuralVariant.getSite1RegionNumber()),
            structuralVariant.getSite1Description(),
            structuralVariant.getSite2EntrezGeneId() == null ? null : Long.toString(structuralVariant.getSite2EntrezGeneId()),
            structuralVariant.getSite2EnsemblTranscriptId(),
            structuralVariant.getSite2Chromosome(),
            Integer.toString(structuralVariant.getSite2Position()),
            structuralVariant.getSite2Contig(),
            structuralVariant.getSite2Region(),
            Integer.toString(structuralVariant.getSite2RegionNumber()),
            structuralVariant.getSite2Description(),
            structuralVariant.getSite2EffectOnFrame(),
            structuralVariant.getNcbiBuild(),
            structuralVariant.getDnaSupport(),
            structuralVariant.getRnaSupport(),
            Integer.toString(structuralVariant.getNormalReadCount()),
            Integer.toString(structuralVariant.getTumorReadCount()),
            Integer.toString(structuralVariant.getNormalVariantCount()),
            Integer.toString(structuralVariant.getTumorVariantCount()),
            Integer.toString(structuralVariant.getNormalPairedEndReadCount()),
            Integer.toString(structuralVariant.getTumorPairedEndReadCount()),
            Integer.toString(structuralVariant.getNormalSplitReadCount()),
            Integer.toString(structuralVariant.getTumorSplitReadCount()),
            structuralVariant.getAnnotation(),
            structuralVariant.getBreakpointType(),
            structuralVariant.getConnectionType(),
            structuralVariant.getEventInfo(),
            structuralVariant.getVariantClass(),
            Integer.toString(structuralVariant.getLength()),
            structuralVariant.getComments(),
            structuralVariant.getSvStatus(),
            structuralVariant.getAnnotationJson()
        );

        if ((structuralVariant.getDriverFilter() != null
            && !structuralVariant.getDriverFilter().isEmpty()
            && !structuralVariant.getDriverFilter().toLowerCase().equals("na"))
            ||
            (structuralVariant.getDriverTiersFilter() != null
            && !structuralVariant.getDriverTiersFilter().isEmpty()
            && !structuralVariant.getDriverTiersFilter().toLowerCase().equals("na"))) {
            ClickHouseBulkLoader.getClickHouseBulkLoader("alteration_driver_annotation").insertRecord(
                Long.toString(structuralVariant.getInternalId()),
                Integer.toString(structuralVariant.getGeneticProfileId()),
                Integer.toString(structuralVariant.getSampleIdInternal()),
                structuralVariant.getDriverFilter(),
                structuralVariant.getDriverFilterAnn(),
                structuralVariant.getDriverTiersFilter(),
                structuralVariant.getDriverTiersFilterAnn()
            );
        }
    }

    public static void deleteStructuralVariants(int geneticProfileId, Set<Integer> sampleIds) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoGene.class);
            if (sampleIds.isEmpty()) {
                return;
            }
            final Connection queryCon = con;
            ClickHouseBulkUploader.upload(sampleIds, stagingTable -> {
                try (PreparedStatement stmt = queryCon.prepareStatement(
                        "DELETE FROM alteration_driver_annotation " +
                        "WHERE genetic_profile_id=? AND sample_id IN (SELECT id FROM " + stagingTable + ")")) {
                    stmt.setInt(1, geneticProfileId);
                    stmt.executeUpdate();
                }
                try (PreparedStatement stmt = queryCon.prepareStatement(
                        "DELETE FROM structural_variant " +
                        "WHERE genetic_profile_id=? AND sample_id IN (SELECT id FROM " + stagingTable + ")")) {
                    stmt.setInt(1, geneticProfileId);
                    stmt.executeUpdate();
                }
                return null;
            });
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoGene.class, con, pstmt, rs);
        }
    }

    public static long getLargestInternalId() throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoMutation.class);
            pstmt = con.prepareStatement("SELECT max(`internal_id`) FROM `structural_variant`");
            rs = pstmt.executeQuery();
            return rs.next() ? rs.getLong(1) : 0;
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoMutation.class, con, pstmt, rs);
        }
    }

    /**
     * Return all structural variants in the database.
     * @return
     * @throws DaoException
     */
    public static List<StructuralVariant> getAllStructuralVariants() throws DaoException {
        ArrayList<StructuralVariant> result = new ArrayList<>();
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoGeneset.class);
            pstmt = con.prepareStatement(
                "SELECT * FROM structural_variant" +
                    " LEFT JOIN alteration_driver_annotation ON" +
                    "  structural_variant.genetic_profile_id = alteration_driver_annotation.genetic_profile_id" +
                    "  and structural_variant.sample_id = alteration_driver_annotation.sample_id" +
                    "  and structural_variant.internal_id = alteration_driver_annotation.alteration_event_id");
            rs = pstmt.executeQuery();

            while (rs.next()) {
                result.add(extractStructuralVariant(rs));
            }
            return result;
        }
        catch (SQLException e) {
            throw new DaoException(e);
        }
        finally {
            JdbcUtil.closeAll(DaoGeneset.class, con, pstmt, rs);
        }
    }

    /**
     * Extracts StructuralVariant record from ResultSet.
     * @param rs
     * @return StructuralVariant record
     */
    private static StructuralVariant extractStructuralVariant(ResultSet rs) throws SQLException {
        StructuralVariant structuralVariant = new StructuralVariant();
        structuralVariant.setGeneticProfileId(rs.getInt("genetic_profile_id"));
        structuralVariant.setSampleIdInternal(rs.getInt("sample_id"));
        structuralVariant.setSite1EntrezGeneId(rs.getLong("site1_entrez_gene_id"));
        structuralVariant.setSite1EnsemblTranscriptId(rs.getString("site1_ensembl_transcript_id"));
        structuralVariant.setSite1Chromosome(rs.getString("site1_chromosome"));
        structuralVariant.setSite1Position(rs.getInt("site1_position"));
        structuralVariant.setSite1Contig(rs.getString("site1_contig"));
        structuralVariant.setSite1Region(rs.getString("site1_region"));
        structuralVariant.setSite1RegionNumber(rs.getInt("site1_region_number"));
        structuralVariant.setSite1Description(rs.getString("site1_description"));
        structuralVariant.setSite2EntrezGeneId(rs.getLong("site2_entrez_gene_id"));
        structuralVariant.setSite2EnsemblTranscriptId(rs.getString("site2_ensembl_transcript_id"));
        structuralVariant.setSite2Chromosome(rs.getString("site2_chromosome"));
        structuralVariant.setSite2Position(rs.getInt("site2_position"));
        structuralVariant.setSite2Contig(rs.getString("site2_contig"));
        structuralVariant.setSite2Region(rs.getString("site2_region"));
        structuralVariant.setSite2RegionNumber(rs.getInt("site2_region_number"));
        structuralVariant.setSite2Description(rs.getString("site2_description"));
        structuralVariant.setSite2EffectOnFrame(rs.getString("site2_effect_on_frame"));
        structuralVariant.setNcbiBuild(rs.getString("ncbi_build"));
        structuralVariant.setDnaSupport(rs.getString("dna_support"));
        structuralVariant.setRnaSupport(rs.getString("rna_support"));
        structuralVariant.setNormalReadCount(rs.getInt("normal_read_count"));
        structuralVariant.setTumorReadCount(rs.getInt("tumor_read_count"));
        structuralVariant.setNormalVariantCount(rs.getInt("normal_variant_count"));
        structuralVariant.setTumorVariantCount(rs.getInt("tumor_variant_count"));
        structuralVariant.setNormalPairedEndReadCount(rs.getInt("normal_paired_end_read_count"));
        structuralVariant.setTumorPairedEndReadCount(rs.getInt("tumor_paired_end_read_count"));
        structuralVariant.setNormalSplitReadCount(rs.getInt("normal_split_read_count"));
        structuralVariant.setTumorSplitReadCount(rs.getInt("tumor_split_read_count"));
        structuralVariant.setAnnotation(rs.getString("annotation"));
        structuralVariant.setBreakpointType(rs.getString("breakpoint_type"));
        structuralVariant.setConnectionType(rs.getString("connection_type"));
        structuralVariant.setEventInfo(rs.getString("event_info"));
        structuralVariant.setVariantClass(rs.getString("class"));
        structuralVariant.setLength(rs.getInt("length"));
        structuralVariant.setComments(rs.getString("comments"));
        structuralVariant.setDriverFilter(rs.getString("driver_filter"));
        structuralVariant.setDriverFilterAnn(rs.getString("driver_filter_annotation"));
        structuralVariant.setDriverTiersFilter(rs.getString("driver_tiers_filter"));
        structuralVariant.setDriverTiersFilterAnn(rs.getString("driver_tiers_filter_annotation"));
        structuralVariant.setSvStatus(rs.getString("sv_status"));
        structuralVariant.setAnnotationJson(rs.getString("annotation_json"));
        return structuralVariant;
    }
}

