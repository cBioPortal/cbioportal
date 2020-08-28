/*
 * Copyright (c) 2017 The Hyve B.V.
 * This code is licensed under the GNU Affero General Public License (AGPL),
 * version 3, or (at your option) any later version.
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
import org.mskcc.cbio.portal.model.StructuralVariant;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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
        MySQLbulkLoader bl =  MySQLbulkLoader.getMySQLbulkLoader("structural_variant");
        String[] fieldNames = new String[]{
                                            "INTERNAL_ID",
                                            "GENETIC_PROFILE_ID",
                                            "SAMPLE_ID",
                                            "SITE1_ENTREZ_GENE_ID",
                                            "SITE1_ENSEMBL_TRANSCRIPT_ID",
                                            "SITE1_EXON",
                                            "SITE1_CHROMOSOME",
                                            "SITE1_POSITION",
                                            "SITE1_DESCRIPTION",
                                            "SITE2_ENTREZ_GENE_ID",
                                            "SITE2_ENSEMBL_TRANSCRIPT_ID",
                                            "SITE2_EXON",
                                            "SITE2_CHROMOSOME",
                                            "SITE2_POSITION",
                                            "SITE2_DESCRIPTION",
                                            "SITE2_EFFECT_ON_FRAME",
                                            "NCBI_BUILD",
                                            "DNA_SUPPORT",
                                            "RNA_SUPPORT",
                                            "NORMAL_READ_COUNT",
                                            "TUMOR_READ_COUNT",
                                            "NORMAL_VARIANT_COUNT",
                                            "TUMOR_VARIANT_COUNT",
                                            "NORMAL_PAIRED_END_READ_COUNT",
                                            "TUMOR_PAIRED_END_READ_COUNT",
                                            "NORMAL_SPLIT_READ_COUNT",
                                            "TUMOR_SPLIT_READ_COUNT",
                                            "ANNOTATION",
                                            "BREAKPOINT_TYPE",
                                            "CENTER",
                                            "CONNECTION_TYPE",
                                            "EVENT_INFO",
                                            "CLASS",
                                            "LENGTH",
                                            "COMMENTS",
                                            "EXTERNAL_ANNOTATION"
                                            };
        bl.setFieldNames(fieldNames);

        // write to the temp file maintained by the MySQLbulkLoader
        bl.insertRecord(
           Long.toString(structuralVariant.getInternalId()),
           Integer.toString(structuralVariant.getGeneticProfileId()),
           Integer.toString(structuralVariant.getSampleIdInternal()),
           Long.toString(structuralVariant.getSite1EntrezGeneId()),
           structuralVariant.getSite1EnsemblTranscriptId(),
           Integer.toString(structuralVariant.getSite1Exon()),
           structuralVariant.getSite1Chromosome(),
           Integer.toString(structuralVariant.getSite1Position()),
           structuralVariant.getSite1Description(),
           Long.toString(structuralVariant.getSite2EntrezGeneId()),
           structuralVariant.getSite2EnsemblTranscriptId(),
           Integer.toString(structuralVariant.getSite2Exon()),
           structuralVariant.getSite2Chromosome(),
           Integer.toString(structuralVariant.getSite2Position()),
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
           structuralVariant.getCenter(),
           structuralVariant.getConnectionType(),
           structuralVariant.getEventInfo(),
           structuralVariant.getVariantClass(),
           Integer.toString(structuralVariant.getLength()),
           structuralVariant.getComments(),
           structuralVariant.getExternalAnnotation());

        if (structuralVariant.getDriverFilter() != null || structuralVariant.getDriverTiersFilter() != null) {
            MySQLbulkLoader.getMySQLbulkLoader("alteration_driver_annotation").insertRecord(
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

    public static long getLargestInternalId() throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoMutation.class);
            pstmt = con.prepareStatement("SELECT MAX(`INTERNAL_ID`) FROM `structural_variant`");
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
                    "  structural_variant.GENETIC_PROFILE_ID = alteration_driver_annotation.GENETIC_PROFILE_ID" +
                    "  and structural_variant.SAMPLE_ID = alteration_driver_annotation.SAMPLE_ID" +
                    "  and structural_variant.INTERNAL_ID = alteration_driver_annotation.ALTERATION_EVENT_ID");
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
        structuralVariant.setGeneticProfileId(rs.getInt("GENETIC_PROFILE_ID"));
        structuralVariant.setSampleIdInternal(rs.getInt("SAMPLE_ID"));
        structuralVariant.setSite1EntrezGeneId(rs.getLong("SITE1_ENTREZ_GENE_ID"));
        structuralVariant.setSite1EnsemblTranscriptId(rs.getString("SITE1_ENSEMBL_TRANSCRIPT_ID"));
        structuralVariant.setSite1Exon(rs.getInt("SITE1_EXON"));
        structuralVariant.setSite1Chromosome(rs.getString("SITE1_CHROMOSOME"));
        structuralVariant.setSite1Position(rs.getInt("SITE1_POSITION"));
        structuralVariant.setSite1Description(rs.getString("SITE1_DESCRIPTION"));
        structuralVariant.setSite2EntrezGeneId(rs.getLong("SITE2_ENTREZ_GENE_ID"));
        structuralVariant.setSite2EnsemblTranscriptId(rs.getString("SITE2_ENSEMBL_TRANSCRIPT_ID"));
        structuralVariant.setSite2Exon(rs.getInt("SITE2_EXON"));
        structuralVariant.setSite2Chromosome(rs.getString("SITE2_CHROMOSOME"));
        structuralVariant.setSite2Position(rs.getInt("SITE2_POSITION"));
        structuralVariant.setSite2Description(rs.getString("SITE2_DESCRIPTION"));
        structuralVariant.setSite2EffectOnFrame(rs.getString("SITE2_EFFECT_ON_FRAME"));
        structuralVariant.setNcbiBuild(rs.getString("NCBI_BUILD"));
        structuralVariant.setDnaSupport(rs.getString("DNA_SUPPORT"));
        structuralVariant.setRnaSupport(rs.getString("RNA_SUPPORT"));
        structuralVariant.setNormalReadCount(rs.getInt("NORMAL_READ_COUNT"));
        structuralVariant.setTumorReadCount(rs.getInt("TUMOR_READ_COUNT"));
        structuralVariant.setNormalVariantCount(rs.getInt("NORMAL_VARIANT_COUNT"));
        structuralVariant.setTumorVariantCount(rs.getInt("TUMOR_VARIANT_COUNT"));
        structuralVariant.setNormalPairedEndReadCount(rs.getInt("NORMAL_PAIRED_END_READ_COUNT"));
        structuralVariant.setTumorPairedEndReadCount(rs.getInt("TUMOR_PAIRED_END_READ_COUNT"));
        structuralVariant.setNormalSplitReadCount(rs.getInt("NORMAL_SPLIT_READ_COUNT"));
        structuralVariant.setTumorSplitReadCount(rs.getInt("TUMOR_SPLIT_READ_COUNT"));
        structuralVariant.setAnnotation(rs.getString("ANNOTATION"));
        structuralVariant.setBreakpointType(rs.getString("BREAKPOINT_TYPE"));
        structuralVariant.setCenter(rs.getString("CENTER"));
        structuralVariant.setConnectionType(rs.getString("CONNECTION_TYPE"));
        structuralVariant.setEventInfo(rs.getString("EVENT_INFO"));
        structuralVariant.setVariantClass(rs.getString("CLASS"));
        structuralVariant.setLength(rs.getInt("LENGTH"));
        structuralVariant.setComments(rs.getString("COMMENTS"));
        structuralVariant.setExternalAnnotation(rs.getString("EXTERNAL_ANNOTATION"));
        structuralVariant.setDriverFilter(rs.getString("DRIVER_FILTER"));
        structuralVariant.setDriverFilterAnn(rs.getString("DRIVER_FILTER_ANNOTATION"));
        structuralVariant.setDriverTiersFilter(rs.getString("DRIVER_TIERS_FILTER"));
        structuralVariant.setDriverTiersFilterAnn(rs.getString("DRIVER_TIERS_FILTER_ANNOTATION"));
        return structuralVariant;
    }
}

