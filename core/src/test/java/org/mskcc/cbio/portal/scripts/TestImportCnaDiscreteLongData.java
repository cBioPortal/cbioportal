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

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mskcc.cbio.portal.dao.*;
import org.mskcc.cbio.portal.model.CnaEvent;
import org.mskcc.cbio.portal.model.GeneticProfile;
import org.mskcc.cbio.portal.model.Sample;
import org.mskcc.cbio.portal.util.StableIdUtil;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:/applicationContext-dao.xml"})
@Rollback
@Transactional
public class TestImportCnaDiscreteLongData {
    int studyId;
    GeneticProfile geneticProfile;
    String genePanel = "TESTPANEL_CNA_DISCRETE_LONG_FORMAT";

    @Before
    public void setUp() throws DaoException {
        studyId = DaoCancerStudy
            .getCancerStudyByStableId("study_tcga_pub")
            .getInternalId();
        this.geneticProfile = DaoGeneticProfile
            .getGeneticProfileByStableId("study_tcga_pub_cna_long");
    }

    @After
    public void cleanUp() throws DaoException {
        MySQLbulkLoader.flushAll();
    }

    /**
     * Test the import of cna data file in long format with 2 samples
     */
    @Test
    public void testImportCnaDiscreteLongDataAddsSamples() throws Exception {
        File file = new File("src/test/resources/data_cna_discrete_import_test.txt");
        new ImportCnaDiscreteLongData(
            file,
            geneticProfile.getGeneticProfileId(),
            genePanel,
            DaoGeneOptimized.getInstance(),
            DaoGeneticAlteration.getInstance()
        ).importData();

        // Test new samples are added:
        List<String> expectedSampleIds = newArrayList("TCGA-A1-A0SB-11", "TCGA-A2-A04U-11");
        for (String id : expectedSampleIds) {
            assertSampleExistsInGeneticProfile(id);
        }
    }

    /**
     * Test the import of cna data file in long format with:
     * - 10 cna events (-2, -1.5 or 2)
     * - 4 non-cna events (0, -1, 1)
     */
    @Test
    public void testImportCnaDiscreteLongDataAddsCnaEvents() throws Exception {
        List<CnaEvent.Event> beforeCnaEvents = DaoCnaEvent.getAllCnaEvents();
        assertEquals(beforeCnaEvents.size(), 17);

        File file = new File("src/test/resources/data_cna_discrete_import_test.txt");
        new ImportCnaDiscreteLongData(
            file,
            geneticProfile.getGeneticProfileId(),
            genePanel,
            DaoGeneOptimized.getInstance(),
            DaoGeneticAlteration.getInstance()
        ).importData();

        List<CnaEvent.Event> resultCnaEvents = DaoCnaEvent.getAllCnaEvents();

        // Test all cna events are added:
        int expectedCnaEventCount = 10;
        int expectedNewCnaEvents = beforeCnaEvents.size() + expectedCnaEventCount;
        assertEquals(
            expectedNewCnaEvents,
            resultCnaEvents.size()
        );

        // Test gene with homozygous deletion and amplification has two cna events:
        List<String> cnaEvents = resultCnaEvents
            .stream()
            .filter(e -> e.getGene().getEntrezGeneId() == 2115)
            .map(e -> e.getAlteration().getDescription())
            .collect(toList());
        assertEquals(2, cnaEvents.size());
        assertTrue(newArrayList("Amplified", "Homozygously deleted").containsAll(cnaEvents));

        // Test gene with partial deletion and amplification has two cna events:
        List<String> convertedCnaEvents = resultCnaEvents
            .stream()
            .filter(e -> e.getGene().getEntrezGeneId() == 3983)
            .map(e -> e.getAlteration().getDescription())
            .collect(toList());
        assertEquals(2, cnaEvents.size());
        assertTrue(newArrayList("Amplified", "Homozygously deleted").containsAll(cnaEvents));

        // Test gene with homozygous deletion and amplification has no cna events:
        List<CnaEvent.Event> skippedCnaEvents = resultCnaEvents
            .stream()
            .filter(e -> e.getGene().getEntrezGeneId() == 56914)
            .collect(toList());
        assertEquals(0, skippedCnaEvents.size());
    }

    /**
     * Test the import of cna data file in long format with 7 genes
     */
    @Test
    public void testImportCnaDiscreteLongDataAddsGeneticAlterations() throws Exception {
        List<TestGeneticAlteration> beforeGeneticAlterations = getAllGeneticAlterations();
        assertEquals(beforeGeneticAlterations.size(), 42);

        File file = new File("src/test/resources/data_cna_discrete_import_test.txt");
        new ImportCnaDiscreteLongData(
            file,
            geneticProfile.getGeneticProfileId(),
            genePanel,
            DaoGeneOptimized.getInstance(),
            DaoGeneticAlteration.getInstance()
        ).importData();

        // Test genetic alterations are added for all genes:
        List<TestGeneticAlteration> resultGeneticAlterations = getAllGeneticAlterations();
        List<Long> expectedEntrezIds = newArrayList(2115L, 27334L, 57670L, 80070L, 3983L, 56914L, 2261L);
        assertEquals(beforeGeneticAlterations.size() + expectedEntrezIds.size(), resultGeneticAlterations.size());
    }

    /**
     * Test the imported events match the imported genetic profile samples
     */
    @Test
    public void testImportCnaDiscreteLongDataAddsGeneticAlterationsAndProfileSamplesInCorrectOrder() throws Exception {
        List<TestGeneticAlteration> beforeGeneticAlterations = getAllGeneticAlterations();
        assertEquals(beforeGeneticAlterations.size(), 42);

        File file = new File("src/test/resources/data_cna_discrete_import_test.txt");
        new ImportCnaDiscreteLongData(
            file,
            geneticProfile.getGeneticProfileId(),
            genePanel,
            DaoGeneOptimized.getInstance(),
            DaoGeneticAlteration.getInstance()
        ).importData();

        // Test order of genetic alteration values:
        TestGeneticAlteration geneticAlteration = getGeneticAlterationBy(2115L);
        assertEquals(geneticProfile.getGeneticProfileId(), geneticAlteration.geneticProfileId);
        assertEquals("2,-2,", geneticAlteration.values);

        // Test order of samples in genetic profile samples matches the order in genetic alteration:
        TestGeneticProfileSample geneticProfileSample = getGeneticProfileSample(geneticProfile.getGeneticProfileId());
        assertEquals("21,20,", geneticProfileSample.orderedSampleList);
    }

    /**
     * Test genetic events are imported, even when not imported as cna event
     */
    @Test
    public void testImportCnaDiscreteLongDataAddsGeneticAlterationsFromNonCnaEvents() throws Exception {
        List<TestGeneticAlteration> beforeGeneticAlterations = getAllGeneticAlterations();
        assertEquals(beforeGeneticAlterations.size(), 42);

        File file = new File("src/test/resources/data_cna_discrete_import_test.txt");
        new ImportCnaDiscreteLongData(
            file,
            geneticProfile.getGeneticProfileId(),
            genePanel,
            DaoGeneOptimized.getInstance(),
            DaoGeneticAlteration.getInstance()
        ).importData();

        // Test genetic alteration are added of non-cna event:
        TestGeneticAlteration geneticAlteration = getGeneticAlterationBy(56914);
        assertEquals(geneticProfile.getGeneticProfileId(), geneticAlteration.geneticProfileId);
        assertEquals("0,1,", geneticAlteration.values);
        TestGeneticProfileSample geneticProfileSample = getGeneticProfileSample(geneticProfile.getGeneticProfileId());
        assertEquals("21,20,", geneticProfileSample.orderedSampleList);
    }

    /**
     * Test genetic events are imported, even when not imported as cna event
     */
    @Test
    public void testImportCnaDiscreteLongDataIgnoresLineWithDuplicateGene() throws Exception {
        List<TestGeneticAlteration> beforeGeneticAlterations = getAllGeneticAlterations();
        assertEquals(beforeGeneticAlterations.size(), 42);

        File file = new File("src/test/resources/data_cna_discrete_import_test.txt");
        new ImportCnaDiscreteLongData(
            file,
            geneticProfile.getGeneticProfileId(),
            genePanel,
            DaoGeneOptimized.getInstance(),
            DaoGeneticAlteration.getInstance()
        ).importData();

        // Test genetic alteration are deduplicated:
        TestGeneticAlteration geneticAlteration = getGeneticAlterationBy(57670);
        assertEquals(geneticProfile.getGeneticProfileId(), geneticAlteration.geneticProfileId);
        // Should not be "2,-2,2" or (2,2):
        assertEquals("2,-2,", geneticAlteration.values);
    }

    /**
     * Test the import of cna data file in long format with:
     * - 3 cna events with pd annotations
     */
    @Test
    public void testImportCnaDiscreteLongDataAddsPdAnnotations() throws Exception {
        File file = new File("src/test/resources/data_cna_discrete_import_test.txt");
        new ImportCnaDiscreteLongData(
            file,
            geneticProfile.getGeneticProfileId(),
            genePanel,
            DaoGeneOptimized.getInstance(),
            DaoGeneticAlteration.getInstance()
        ).importData();
        List<Long> genes = newArrayList(3983L, 27334L, 2115L);
        List<CnaEvent.Event> resultCnaEvents = DaoCnaEvent.getAllCnaEvents()
            .stream()
            .filter(event -> genes.contains(event.getGene().getEntrezGeneId()))
            .collect(toList());
        String sample = "TCGA-A2-A04U-11";
        List<TestPdAnnotation> allCnaPdAnnotations = getAllCnaPdAnnotations(createPrimaryKeys(sample, resultCnaEvents));
        assertEquals(3, allCnaPdAnnotations.size());
        String allDriverTiersFilters = allCnaPdAnnotations
            .stream()
            .map(a -> a.driverTiersFilter)
            .collect(joining(","));
        assertEquals("Class 2,Class 1,NA", allDriverTiersFilters);
    }

    private List<TestPdAnnotationPK> createPrimaryKeys(String sample, List<CnaEvent.Event> cnaEvents) {
        return cnaEvents.stream().map(e -> {
            TestPdAnnotationPK pk = new TestPdAnnotationPK();
            pk.geneticProfileId = geneticProfile.getGeneticProfileId();
            pk.sampleId = DaoSample.getSampleByCancerStudyAndSampleId(
                geneticProfile.getCancerStudyId(),
                StableIdUtil.getSampleId(sample)
            ).getInternalId();
            pk.alterationEventId = e.getEventId();
            return pk;
        }).collect(toList());
    }

    private void assertSampleExistsInGeneticProfile(String sampleId) throws DaoException {
        String sampleStableId = StableIdUtil.getSampleId(sampleId);

        Sample sample = DaoSample.getSampleByCancerStudyAndSampleId(
            geneticProfile.getCancerStudyId(),
            sampleStableId
        );
        Assert.assertNotNull(sample);
        Assert.assertTrue(DaoSampleProfile.sampleExistsInGeneticProfile(
            sample.getInternalId(),
            geneticProfile.getGeneticProfileId()
        ));
    }

    private List<TestPdAnnotation> getAllCnaPdAnnotations(List<TestPdAnnotationPK> pks) throws DaoException {
        List<String> pkStrings = new ArrayList<>();
        for (TestPdAnnotationPK pk : pks) {
            pkStrings.add(String.format(
                "( ALTERATION_EVENT_ID=%d AND GENETIC_PROFILE_ID=%d AND SAMPLE_ID=%d )",
                pk.alterationEventId, pk.geneticProfileId, pk.sampleId
            ));
        }


        String q = "SELECT DRIVER_TIERS_FILTER_ANNOTATION, DRIVER_TIERS_FILTER, DRIVER_FILTER_ANNOTATION, DRIVER_FILTER, "
            + "ALTERATION_EVENT_ID, GENETIC_PROFILE_ID, SAMPLE_ID "
            + "FROM alteration_driver_annotation ";
        if (pks.size() > 0) {
            q += "WHERE " + String.join(" OR ", pkStrings);
        }

        System.out.println("pkq:" + q);
        return query(
            q,
            (ResultSet rs) -> {
                TestPdAnnotation line = new TestPdAnnotation();
                line.driverFilter = rs.getString("DRIVER_FILTER");
                line.driverTiersFilter = rs.getString("DRIVER_TIERS_FILTER");
                line.driverFilterAnnotation = rs.getString("DRIVER_FILTER_ANNOTATION");
                line.driverTiersFilterAnnotation = rs.getString("DRIVER_TIERS_FILTER_ANNOTATION");

                line.pk = new TestPdAnnotationPK();
                line.pk.alterationEventId = rs.getInt("ALTERATION_EVENT_ID");
                line.pk.geneticProfileId = rs.getInt("GENETIC_PROFILE_ID");
                line.pk.sampleId = rs.getInt("SAMPLE_ID");

                return line;
            });
    }

    private List<TestGeneticAlteration> getAllGeneticAlterations() throws DaoException {
        return query("SELECT * FROM genetic_alteration", (ResultSet rs) -> {
            TestGeneticAlteration line = new TestGeneticAlteration();
            line.geneticProfileId = rs.getInt("GENETIC_PROFILE_ID");
            line.geneticEntityId = rs.getInt("GENETIC_ENTITY_ID");
            line.values = rs.getString("VALUES");
            return line;
        });
    }

    private TestGeneticAlteration getGeneticAlterationBy(long entrezId) throws DaoException {
        return query("SELECT ga.GENETIC_PROFILE_ID, ga.GENETIC_ENTITY_ID, ga.VALUES, g.ENTREZ_GENE_ID " +
                "FROM genetic_alteration AS ga " +
                "RIGHT JOIN gene AS g " +
                "ON g.GENETIC_ENTITY_ID = ga.GENETIC_ENTITY_ID " +
                "WHERE g.ENTREZ_GENE_ID=" + entrezId,
            (ResultSet rs) -> {
                TestGeneticAlteration line = new TestGeneticAlteration();
                line.geneticProfileId = rs.getInt("GENETIC_PROFILE_ID");
                line.geneticEntityId = rs.getInt("GENETIC_ENTITY_ID");
                line.values = rs.getString("VALUES");
                line.entrezId = rs.getLong("ENTREZ_GENE_ID");
                return line;
            }).get(0);
    }

    private TestGeneticProfileSample getGeneticProfileSample(long profileId) throws DaoException {
        return query(
            "SELECT * FROM genetic_profile_samples WHERE GENETIC_PROFILE_ID=" + profileId,
            (ResultSet rs) -> {
                TestGeneticProfileSample line = new TestGeneticProfileSample();
                line.geneticProfileId = rs.getInt("GENETIC_PROFILE_ID");
                line.orderedSampleList = rs.getString("ORDERED_SAMPLE_LIST");
                return line;
            }).get(0);
    }

    private <T> List<T> query(String query, FunctionThrowsSql<ResultSet, T> handler) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<T> result = new ArrayList<>();
        try {
            con = JdbcUtil.getDbConnection(DaoGeneticAlteration.class);
            pstmt = con.prepareStatement(query);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                result.add(handler.apply(rs));
            }
            return result;
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoGeneticAlteration.class, con, pstmt, rs);
        }
    }

}

class TestGeneticAlteration {
    public int geneticProfileId;
    public int geneticEntityId;
    public String values;
    public long entrezId;
}

class TestGeneticProfileSample {
    public int geneticProfileId;
    public String orderedSampleList;
}

class TestPdAnnotationPK {
    public long alterationEventId;
    public int sampleId;
    public int geneticProfileId;
}

class TestPdAnnotation {
    public TestPdAnnotationPK pk;
    public String driverTiersFilterAnnotation;
    public String driverTiersFilter;
    public String driverFilterAnnotation;
    public String driverFilter;
}

@FunctionalInterface
interface FunctionThrowsSql<T, R> {
    R apply(T t) throws SQLException;
}
