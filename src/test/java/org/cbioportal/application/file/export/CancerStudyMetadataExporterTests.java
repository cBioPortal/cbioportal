package org.cbioportal.application.file.export;

import org.cbioportal.application.file.export.exporters.CancerStudyMetadataExporter;
import org.cbioportal.application.file.export.services.CancerStudyMetadataService;
import org.cbioportal.application.file.model.CancerStudyMetadata;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CancerStudyMetadataExporterTests {
    String studyId = "STUDY_ID";
    CancerStudyMetadataService cancerStudyMetadataService = new CancerStudyMetadataService(null) {
        @Override
        public CancerStudyMetadata getCancerStudyMetadata(String studyId) {
            var cancerStudyMetadata = new CancerStudyMetadata();
            cancerStudyMetadata.setCancerStudyIdentifier(studyId);
            cancerStudyMetadata.setTypeOfCancer("Breast Cancer");
            cancerStudyMetadata.setCitation("Foo et al. 2023");
            cancerStudyMetadata.setGroups("Group1, Group2");
            cancerStudyMetadata.setName("Breast Cancer Study");
            cancerStudyMetadata.setDescription("A study on breast cancer");
            cancerStudyMetadata.setAddGlobalCaseList(true);
            cancerStudyMetadata.setPmid("12345678");
            cancerStudyMetadata.setReferenceGenome("GRCh38");
            return cancerStudyMetadata;
        }
    };

    @Test
    public void testNoData() {
        var factory = new InMemoryFileWriterFactory();
        CancerStudyMetadataExporter exporter = new CancerStudyMetadataExporter(new CancerStudyMetadataService(null) {
            @Override
            public CancerStudyMetadata getCancerStudyMetadata(String studyId) {
                return null;
            }
        });

        boolean exported = exporter.exportData(factory, studyId);

        assertFalse("No data should be exported", exported);
        var fileContents = factory.getFileContents();
        assertTrue(fileContents.isEmpty());
    }

    @Test
    public void testExport() {
        var factory = new InMemoryFileWriterFactory();
        CancerStudyMetadataExporter exporter = new CancerStudyMetadataExporter(cancerStudyMetadataService);

        boolean exported = exporter.exportData(factory, studyId);

        assertTrue("Data should be exported", exported);
        var fileContents = factory.getFileContents();
        assertEquals(1, fileContents.size());
        assertTrue(fileContents.containsKey("meta_study.txt"));
        assertEquals("cancer_study_identifier: STUDY_ID\n" + "type_of_cancer: Breast Cancer\n" + "name: Breast Cancer Study\n" + "description: A study on breast cancer\n" + "citation: Foo et al. 2023\n" + "pmid: 12345678\n" + "groups: Group1, Group2\n" + "add_global_case_list: true\n" + "reference_genome: GRCh38\n", fileContents.get("meta_study.txt").toString());
    }
}
