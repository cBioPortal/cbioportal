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

import com.fasterxml.jackson.databind.*;
import org.junit.*;
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
import java.util.*;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.cbioportal.model.MolecularProfile.DataType.DISCRETE;
import static org.cbioportal.model.MolecularProfile.ImportType.DISCRETE_LONG;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@Ignore

@ContextConfiguration(locations = {"classpath:/applicationContext-dao.xml"})
@Rollback
@Transactional
public class TestImportCnaDiscreteLongData {
    int studyId;
    GeneticProfile geneticProfile;
    String genePanel = "TESTPANEL_CNA_DISCRETE_LONG_FORMAT";
    Set<String> noNamespaces = new HashSet<>();

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
            DaoGeneticAlteration.getInstance(),
            noNamespaces).importData();

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
            DaoGeneticAlteration.getInstance(),
            noNamespaces
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
            DaoGeneticAlteration.getInstance(),
            noNamespaces).importData();

        // Test genetic alterations are added for all genes:
        List<TestGeneticAlteration> resultGeneticAlterations = getAllGeneticAlterations();
        List<Long> expectedEntrezIds = newArrayList(2115L, 27334L, 57670L, 80070L, 3983L, 56914L, 2261L);
        assertEquals(beforeGeneticAlterations.size() + expectedEntrezIds.size(), resultGeneticAlterations.size());
    }

    /**
     * Missing cna events should be inserted in genetic_events table as missing value:
     * - data file containing two samples and two genes;
     * - missing combination: gene ETV1 * sample TCGA-A2-A04U-11
     */
    @Test
    public void testImportCnaDiscreteLongDataAddsMissingGeneticAlterations() throws Exception {
        List<TestGeneticAlteration> beforeGeneticAlterations = getAllGeneticAlterations();
        assertEquals(beforeGeneticAlterations.size(), 42);

        File file = new File("src/test/resources/data_cna_discrete_import_test_with_cna_events_missing.txt");
        new ImportCnaDiscreteLongData(
            file,
            geneticProfile.getGeneticProfileId(),
            genePanel,
            DaoGeneOptimized.getInstance(),
            DaoGeneticAlteration.getInstance(),
            noNamespaces).importData();

        // Test genetic alteration are added of non-cna event:
        TestGeneticAlteration geneticAlteration = getGeneticAlterationByEntrez(2115);
        assertEquals(geneticProfile.getGeneticProfileId(), geneticAlteration.geneticProfileId);
        TestGeneticProfileSample geneticProfileSample = getGeneticProfileSample(geneticProfile.getGeneticProfileId());
        assertEquals("21,20,", geneticProfileSample.orderedSampleList);
        assertEquals(getSampleStableIdFromInternalId(21), "TCGA-A1-A0SB-11");
        assertEquals(getSampleStableIdFromInternalId(20), "TCGA-A2-A04U-11");
        // Sample TCGA-A1-A0SB-11 has value 2, and TCGA-A2-A04U-11 is missing:
        assertEquals("2,,", geneticAlteration.values);
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
            DaoGeneticAlteration.getInstance(),
            noNamespaces).importData();

        // Test order of genetic alteration values:
        TestGeneticAlteration geneticAlteration = getGeneticAlterationByEntrez(2115L);
        assertEquals(geneticProfile.getGeneticProfileId(), geneticAlteration.geneticProfileId);
        assertEquals("2,-2,", geneticAlteration.values);

        // Test order of samples in genetic profile samples matches the order in genetic alteration:
        TestGeneticProfileSample geneticProfileSample = getGeneticProfileSample(geneticProfile.getGeneticProfileId());
        assertEquals("21,20,", geneticProfileSample.orderedSampleList);
    }

    /**
     * Test that entries are imported when valid entrez IDs are missing but Hugo IDs are provided
     */
    @Test
    public void testImportCnaDiscreteLongDataHandlesEntriesWithoutEntrezButWithHugo() throws Exception {
        List<TestGeneticAlteration> beforeGeneticAlterations = getAllGeneticAlterations();
        assertEquals(beforeGeneticAlterations.size(), 42);

        File file = new File("src/test/resources/data_cna_discrete_import_test_without_entrez_with_hugo.txt");
        new ImportCnaDiscreteLongData(
            file,
            geneticProfile.getGeneticProfileId(),
            genePanel,
            DaoGeneOptimized.getInstance(),
            DaoGeneticAlteration.getInstance(),
            noNamespaces).importData();

        // Test order of genetic alteration values:
        TestGeneticAlteration geneticAlteration = getGeneticAlterationByEntrez(57670L);
        assertEquals(geneticProfile.getGeneticProfileId(), geneticAlteration.geneticProfileId);
        assertEquals("2,-2,", geneticAlteration.values);
    }

    /**
     * Test that entries are imported when invalid entrez IDs and valid Hugo IDs are provided
     */
    @Test
    public void testImportCnaDiscreteLongDataHandlesEntriesWithWrongEntrezAndCorrectHugo() throws Exception {
        List<TestGeneticAlteration> beforeGeneticAlterations = getAllGeneticAlterations();
        assertEquals(beforeGeneticAlterations.size(), 42);

        File file = new File("src/test/resources/data_cna_discrete_import_test_with_wrong_entrez_and_correct_hugo.txt");
        new ImportCnaDiscreteLongData(
            file,
            geneticProfile.getGeneticProfileId(),
            genePanel,
            DaoGeneOptimized.getInstance(),
            DaoGeneticAlteration.getInstance(),
            noNamespaces).importData();

        // Test order of genetic alteration values:
        TestGeneticAlteration geneticAlteration = getGeneticAlterationByEntrez(57670L);
        assertEquals(geneticProfile.getGeneticProfileId(), geneticAlteration.geneticProfileId);
        assertEquals("2,-2,", geneticAlteration.values);
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
            DaoGeneticAlteration.getInstance(),
            noNamespaces).importData();

        // Test genetic alteration are added of non-cna event:
        TestGeneticAlteration geneticAlteration = getGeneticAlterationByEntrez(56914);
        assertEquals(geneticProfile.getGeneticProfileId(), geneticAlteration.geneticProfileId);
        TestGeneticProfileSample geneticProfileSample = getGeneticProfileSample(geneticProfile.getGeneticProfileId());
        assertEquals("21,20,", geneticProfileSample.orderedSampleList);
        assertEquals(getSampleStableIdFromInternalId(21), "TCGA-A1-A0SB-11");
        assertEquals(getSampleStableIdFromInternalId(20), "TCGA-A2-A04U-11");
        // Sample TCGA-A1-A0SB-11 has value 1, and TCGA-A2-A04U-11 has value 0:
        assertEquals("1,0,", geneticAlteration.values);
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
            DaoGeneticAlteration.getInstance(),
            noNamespaces).importData();

        // Test genetic alteration are deduplicated:
        TestGeneticAlteration geneticAlteration = getGeneticAlterationByEntrez(57670);
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
            DaoGeneticAlteration.getInstance(),
            noNamespaces
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

    @Test
    public void testImportCnaDiscreteLongData_changesProfileDatatypeFromDiscreteLongToDiscrete() throws Exception {
        File file = new File("src/test/resources/data_cna_discrete_import_test.txt");
        
        String startInputDatatype = getGeneticProfileDatatype(this.geneticProfile.getGeneticProfileId());
        assertEquals(DISCRETE_LONG.name(), startInputDatatype);

        new ImportCnaDiscreteLongData(
            file,
            geneticProfile.getGeneticProfileId(),
            genePanel,
            DaoGeneOptimized.getInstance(),
            DaoGeneticAlteration.getInstance(),
            noNamespaces
        ).importData();

        String resultDatatype = getGeneticProfileDatatype(this.geneticProfile.getGeneticProfileId());
        assertEquals(DISCRETE.name(), resultDatatype);
    }


    /**
     * Test the import of cna data file in long format with:
     * - two custom namespaces that should be imported
     * - one unknown namespace that should be ignored
     */
    @Test
    public void testImportCnaDiscreteLongDataOnlyAddsSpecifiedCustomNamespaceColumns() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        File file = new File("src/test/resources/data_cna_discrete_import_test_with_namespaces.txt");
        Set<String> namespacesToImport = newHashSet("MyNamespace", "MyNamespace2");
        new ImportCnaDiscreteLongData(
            file,
            geneticProfile.getGeneticProfileId(),
            genePanel,
            DaoGeneOptimized.getInstance(),
            DaoGeneticAlteration.getInstance(),
            namespacesToImport
        ).importData();

        // All namespace columns provided:
        List<NamespaceAnnotationJson> results = getAnnotationJsonBy(geneticProfile.getGeneticProfileId());
        assertEquals(2, results.size());
        String resultAnnotationJson = results
            .stream()
            .filter(r -> r.entrezGeneId == 2115L)
            .map(r -> r.annotationJson)
            .findFirst().get();
        
        // Namespace 'InvalidNamespace' should be ignored:
        String expectedAnnotationJson = "{\"MyNamespace\": {\"column1\": \"MyValue1\", \"column2\": \"MyValue2\"}, \"MyNamespace2\": {\"blarp\": \"blorp\"}}";
        assertEquals(
            mapper.readTree(results.get(0).annotationJson), 
            mapper.readTree(expectedAnnotationJson)
        );
        assertEquals(
            mapper.readTree(expectedAnnotationJson), 
            mapper.readTree(resultAnnotationJson)
        );
    }

    /**
     * Test the import of cna data file in long format with:
     * - one row with both namespaces
     * - one row with a missing namespace, imported as null
     */
    @Test
    public void testImportCnaDiscreteLongDataImportsMissingNamespacesAsNull() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        File file = new File("src/test/resources/data_cna_discrete_import_test_with_namespaces.txt");
        Set<String> namespacesToImport = newHashSet("MyNamespace", "MyNamespace2");
        new ImportCnaDiscreteLongData(
            file,
            geneticProfile.getGeneticProfileId(),
            genePanel,
            DaoGeneOptimized.getInstance(),
            DaoGeneticAlteration.getInstance(),
            namespacesToImport
        ).importData();

        // All namespace columns provided:
        List<NamespaceAnnotationJson> results = getAnnotationJsonBy(geneticProfile.getGeneticProfileId());
        assertEquals(2, results.size());
        String resultAnnotationJson = results
            .stream()
            .filter(r -> r.entrezGeneId == 2115L)
            .map(r -> r.annotationJson)
            .findFirst().get();
        String expectedAnnotationJson = "{\"MyNamespace\": {\"column1\": \"MyValue1\", \"column2\": \"MyValue2\"}, \"MyNamespace2\": {\"blarp\": \"blorp\"}}";
        assertEquals(
            mapper.readTree(results.get(0).annotationJson),
            mapper.readTree(expectedAnnotationJson)
        );
        assertEquals(
            mapper.readTree(expectedAnnotationJson),
            mapper.readTree(resultAnnotationJson)
        );

        // Only one namespace column provided:
        resultAnnotationJson = results
            .stream()
            .filter(r -> r.entrezGeneId == 27334L)
            .map(r -> r.annotationJson)
            .findFirst().get();
        expectedAnnotationJson = "{\"MyNamespace\": {\"column1\": null, \"column2\": null}, \"MyNamespace2\": {\"blarp\": \"bloerp\"}}";
        assertEquals(
            mapper.readTree(expectedAnnotationJson),
            mapper.readTree(resultAnnotationJson)
        );
    }
    
    /**
     * Test the import of cna data file in long format with:
     * - two rows with the same gene and two different samples each with their own annotationJson 
     */
    @Test
    public void testImportCnaDiscreteLongDataAddsCustomNamespaceColumnsForEachSample() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        File file = new File("src/test/resources/data_cna_discrete_import_test_with_namespaces2.txt");
        Set<String> namespaces = newHashSet("MyNamespace", "MyNamespace2");
        new ImportCnaDiscreteLongData(
            file,
            geneticProfile.getGeneticProfileId(),
            genePanel,
            DaoGeneOptimized.getInstance(),
            DaoGeneticAlteration.getInstance(),
            namespaces
        ).importData();

        List<NamespaceAnnotationJson> results = getAnnotationJsonBy(geneticProfile.getGeneticProfileId());
        assertEquals(2, results.size());
        String expectedAnnotationJsonA1 = "{\"MyNamespace\": {\"column1\": \"v1a\", \"column2\": \"v2a\"}, \"MyNamespace2\": {\"blarp\": \"bloerp\"}}";
        String expectedAnnotationJsonA2 = "{\"MyNamespace\": {\"column1\": \"v1b\", \"column2\": \"v2b\"}, \"MyNamespace2\": {\"blarp\": \"bleurp\"}}";
        
        String resultAnnotationJsonA1 = results
            .stream()
            .filter(r -> "TCGA-A1-A0SB-11".equals(r.stableId))
            .map(r -> r.annotationJson)
            .findFirst().get();
        assertEquals(
            mapper.readTree(expectedAnnotationJsonA1), 
            mapper.readTree(resultAnnotationJsonA1)
        );
        String resultAnnotationJsonA2 = results
            .stream()
            .filter(r -> "TCGA-A2-A04U-11".equals(r.stableId))
            .map(r -> r.annotationJson)
            .findFirst().get();
        assertEquals(
            mapper.readTree(expectedAnnotationJsonA2), 
            mapper.readTree(resultAnnotationJsonA2)
        );
    }
    /**
     * Test the import of cna data file in long format with:
     * - two rows with the same gene and two different samples each with their own annotationJson 
     */
    @Test
    public void testImportCnaDiscreteLongDataImportsCustomNamespaceColumnsAsNullWhenMissing() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        File file = new File("src/test/resources/data_cna_discrete_import_test_without_namespaces.txt");
        Set<String> namespaces = newHashSet("MyNamespace", "MyNamespace2");
        new ImportCnaDiscreteLongData(
            file,
            geneticProfile.getGeneticProfileId(),
            genePanel,
            DaoGeneOptimized.getInstance(),
            DaoGeneticAlteration.getInstance(),
            namespaces
        ).importData();

        List<NamespaceAnnotationJson> results = getAnnotationJsonBy(geneticProfile.getGeneticProfileId());
        assertEquals(1, results.size());

        String expectedAnnotationJson = null;
        assertEquals(expectedAnnotationJson, results.get(0).annotationJson);
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
    
    private String getGeneticProfileDatatype(long geneticProfileId) throws DaoException {
        return runSelectQuery(
            "select DATATYPE " +
                "FROM genetic_profile " +
                "WHERE GENETIC_PROFILE_ID=" + geneticProfileId + " ;",
            (ResultSet rs) -> rs.getString("DATATYPE")
        ).get(0);
    }

    class NamespaceAnnotationJson {
        public int cnaEventId;
        public int sampleId;
        public int entrezGeneId;
        public String stableId;
        public String annotationJson;
    }

    private List<NamespaceAnnotationJson> getAnnotationJsonBy(long geneticProfileId) throws DaoException {
        return runSelectQuery(
            "select cna_event.CNA_EVENT_ID, SAMPLE_ID, STABLE_ID, GENETIC_PROFILE_ID, ENTREZ_GENE_ID, ANNOTATION_JSON " +
                "FROM sample_cna_event " +
                "LEFT JOIN sample ON sample.INTERNAL_ID = sample_cna_event.SAMPLE_ID " +
                "LEFT JOIN cna_event ON cna_event.CNA_EVENT_ID = sample_cna_event.CNA_EVENT_ID " +
                "WHERE GENETIC_PROFILE_ID=" + geneticProfileId + " ;",
            (ResultSet rs) -> {
                NamespaceAnnotationJson result = new NamespaceAnnotationJson();
                result.cnaEventId = rs.getInt("CNA_EVENT_ID");
                result.sampleId = rs.getInt("SAMPLE_ID");
                result.stableId = rs.getString("STABLE_ID");
                result.entrezGeneId = rs.getInt("ENTREZ_GENE_ID");
                result.annotationJson = rs.getString("ANNOTATION_JSON");
                return result;
            });
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

        return runSelectQuery(
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
        return runSelectQuery("SELECT ga.*, g.HUGO_GENE_SYMBOL FROM genetic_alteration as ga left join gene as g on ga.GENETIC_ENTITY_ID=g.GENETIC_ENTITY_ID", (ResultSet rs) -> {
            TestGeneticAlteration line = new TestGeneticAlteration();
            line.geneticProfileId = rs.getInt("GENETIC_PROFILE_ID");
            line.geneticEntityId = rs.getInt("GENETIC_ENTITY_ID");
            line.values = rs.getString("VALUES");
            line.hugoGeneSymbol = rs.getString("HUGO_GENE_SYMBOL");
            return line;
        });
    }

    private TestGeneticAlteration getGeneticAlterationByEntrez(long entrezId) throws DaoException {
        return runSelectQuery("SELECT ga.GENETIC_PROFILE_ID, ga.GENETIC_ENTITY_ID, ga.VALUES, g.HUGO_GENE_SYMBOL " +
                "FROM genetic_alteration AS ga " +
                "RIGHT JOIN gene AS g " +
                "ON g.GENETIC_ENTITY_ID = ga.GENETIC_ENTITY_ID " +
                "WHERE g.ENTREZ_GENE_ID=" + entrezId,
            (ResultSet rs) -> {
                TestGeneticAlteration line = new TestGeneticAlteration();
                line.geneticProfileId = rs.getInt("GENETIC_PROFILE_ID");
                line.geneticEntityId = rs.getInt("GENETIC_ENTITY_ID");
                line.values = rs.getString("VALUES");
                line.hugoGeneSymbol = rs.getString("HUGO_GENE_SYMBOL");
                return line;
            }).get(0);
    }

    private TestGeneticProfileSample getGeneticProfileSample(long profileId) throws DaoException {
        return runSelectQuery(
            "SELECT * FROM genetic_profile_samples WHERE GENETIC_PROFILE_ID=" + profileId,
            (ResultSet rs) -> {
                TestGeneticProfileSample line = new TestGeneticProfileSample();
                line.geneticProfileId = rs.getInt("GENETIC_PROFILE_ID");
                line.orderedSampleList = rs.getString("ORDERED_SAMPLE_LIST");
                return line;
            }).get(0);
    }

    private String getSampleStableIdFromInternalId(Integer internalSampleId) throws DaoException {
        return runSelectQuery(
            "select STABLE_ID from sample where INTERNAL_ID = " + internalSampleId,
            (ResultSet rs) -> rs.getString("STABLE_ID")
        ).get(0);
    }

    private <T> List<T> runSelectQuery(String query, FunctionThrowsSql<ResultSet, T> handler) throws DaoException {
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
    public String hugoGeneSymbol;
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
