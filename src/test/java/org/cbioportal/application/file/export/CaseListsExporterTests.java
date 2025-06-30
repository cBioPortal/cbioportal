package org.cbioportal.application.file.export;

import org.cbioportal.application.file.export.exporters.CaseListsExporter;
import org.cbioportal.application.file.export.services.CaseListMetadataService;
import org.cbioportal.application.file.model.CaseListMetadata;
import org.junit.Test;

import java.util.LinkedHashSet;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CaseListsExporterTests {

    String studyId = "STUDY_ID";

    @Test
    public void testNoCaseLists() {
        var factory = new InMemoryFileWriterFactory();
        CaseListsExporter exporter = new CaseListsExporter(new CaseListMetadataService(null) {
            @Override
            public List<CaseListMetadata> getCaseListsMetadata(String studyId) {
                return List.of();
            }
        });

        boolean exported = exporter.exportData(factory, studyId);

        assertTrue("No case lists should be exported", !exported);
        assertTrue("No files should be created", factory.getFileContents().isEmpty());
    }

    @Test
    public void testExportCaseLists() {
        var factory = new InMemoryFileWriterFactory();
        CaseListsExporter exporter = new CaseListsExporter(caseListMetadataService);

        boolean exported = exporter.exportData(factory, studyId);

        var fileContents = factory.getFileContents();
        assertTrue("Case lists should be exported", exported);
        assertEquals(2, fileContents.size());
        assertTrue(fileContents.containsKey("case_lists/cases_stable_id_1.txt"));
        assertTrue(fileContents.containsKey("case_lists/cases_stable_id_2.txt"));

        assertEquals("cancer_study_identifier: STUDY_ID\n"
                + "stable_id: " + studyId + "_stable_id_1\n"
                + "case_list_name: Case List 1\n"
                + "case_list_description: Description for Case List 1\n"
                + "case_list_ids: SAMPLE_1\tSAMPLE_2\n",
            fileContents.get("case_lists/cases_stable_id_1.txt").toString()); //note: the study id is excluded from the stable id in the file name

        assertEquals("cancer_study_identifier: STUDY_ID\n"
                + "stable_id: stable_id_2\n"
                + "case_list_name: Case List 2\n"
                + "case_list_description: Description for Case List 2\n"
                + "case_list_ids: SAMPLE_3\tSAMPLE_4\n",
            fileContents.get("case_lists/cases_stable_id_2.txt").toString());
    }

    CaseListMetadataService caseListMetadataService = new CaseListMetadataService(null) {
        @Override
        public List<CaseListMetadata> getCaseListsMetadata(String studyId) {
            var caseList1 = new CaseListMetadata();
            caseList1.setCancerStudyIdentifier(studyId);
            caseList1.setStableId(studyId + "_" +"stable_id_1");
            caseList1.setName("Case List 1");
            caseList1.setDescription("Description for Case List 1");
            caseList1.setSampleIds(new LinkedHashSet<>(List.of("SAMPLE_1", "SAMPLE_2")));

            var caseList2 = new CaseListMetadata();
            caseList2.setCancerStudyIdentifier(studyId);
            caseList2.setStableId("stable_id_2");
            caseList2.setName("Case List 2");
            caseList2.setDescription("Description for Case List 2");
            caseList2.setSampleIds(new LinkedHashSet<>(List.of("SAMPLE_3", "SAMPLE_4")));

            return List.of(caseList1, caseList2);
        }
    };
}