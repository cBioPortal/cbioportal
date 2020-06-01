/*
 * Copyright (c) 2018 The Hyve B.V.
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
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
*/

/*
 * @author Sander Tan
*/

package org.mskcc.cbio.portal.scripts;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mskcc.cbio.portal.dao.*;
import org.mskcc.cbio.portal.model.*;
import org.mskcc.cbio.portal.util.*;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;
import static org.junit.Assert.*;
import java.io.*;
import java.sql.*;

/**
 * Test class to test functionality of ImportStructralVariantData
*/
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/applicationContext-dao.xml" })
@Rollback
@Transactional
public class TestImportStructuralVariantData{
    int studyId;
    int geneticProfileId;

    /**
     * Extracts StructuralVariant record from ResultSet.
     * @param rs
     * @return StructuralVariant record
     * @throws SQLException
     * @throws DaoException
     */
    private StructuralVariant extractStructuralVariant(ResultSet rs) throws SQLException, DaoException {
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

    @Before
    public void setUp() throws DaoException
    {
        studyId = DaoCancerStudy.getCancerStudyByStableId("study_tcga_pub").getInternalId();
        geneticProfileId = DaoGeneticProfile.getGeneticProfileByStableId("study_tcga_pub_structural_variants").getGeneticProfileId();
    }

    @Test
    public void testImportStructuralVariantData() throws DaoException, IOException {
        ProgressMonitor.setConsoleMode(false);

        // Load test structural variants
        File file = new File("src/test/resources/data_structural_variants.txt");
        ImportStructuralVariantData importer = new ImportStructuralVariantData(file, geneticProfileId, null);
        importer.importData();
        MySQLbulkLoader.flushAll();

        // Retrieve all imported structural variants
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoGeneset.class);
            pstmt = con.prepareStatement("SELECT * FROM structural_variant");
            rs = pstmt.executeQuery();

            // Test first structural variant entry
            rs.next();
            StructuralVariant structuralVariant = extractStructuralVariant(rs);
            assertEquals("KIAA1549-BRAF.K16B10.COSF509_2", structuralVariant.getSite2Description());

            // Test second structural variant entry
            rs.next();
            structuralVariant = extractStructuralVariant(rs);
            assertEquals("ENST00000318522", structuralVariant.getSite1EnsemblTranscriptId());
        }
        catch (SQLException e) {
            throw new DaoException(e);
        }
        finally {
            JdbcUtil.closeAll(DaoGeneset.class, con, pstmt, rs);
        }
    }
}
