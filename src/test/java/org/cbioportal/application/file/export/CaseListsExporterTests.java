package org.cbioportal.application.file.export;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.cbioportal.application.file.export.exporters.CaseListsExporter;
import org.cbioportal.application.file.export.exporters.ExportDetails;
import org.cbioportal.application.file.export.services.CaseListMetadataService;
import org.cbioportal.application.file.model.CaseListMetadata;
import org.junit.Test;

public class CaseListsExporterTests {

  ExportDetails exportDetails = new ExportDetails("STUDY_ID");
  CaseListMetadataService caseListMetadataService =
      new CaseListMetadataService(null) {
        @Override
        public List<CaseListMetadata> getCaseListsMetadata(String studyId, Set<String> sampleIds) {
          assertEquals("STUDY_ID", studyId);

          var caseList1 = new CaseListMetadata();
          caseList1.setCancerStudyIdentifier(studyId);
          caseList1.setStableId(studyId + "_" + "stable_id_1");
          caseList1.setName("Case List 1");
          caseList1.setDescription("Description for Case List 1");
          caseList1.setSampleIds(new LinkedHashSet<>(List.of("SAMPLE_1", "SAMPLE_2")));

          var caseList2 = new CaseListMetadata();
          caseList2.setCancerStudyIdentifier(studyId);
          caseList2.setStableId(studyId + "_" + "stable_id_2");
          caseList2.setName("Case List 2");
          caseList2.setDescription("Description for Case List 2");
          caseList2.setSampleIds(new LinkedHashSet<>(List.of("SAMPLE_3", "SAMPLE_4")));

          return List.of(caseList1, caseList2);
        }
      };

  @Test
  public void testNoCaseLists() {
    var factory = new InMemoryFileWriterFactory();
    CaseListsExporter exporter =
        new CaseListsExporter(
            new CaseListMetadataService(null) {
              @Override
              public List<CaseListMetadata> getCaseListsMetadata(
                  String studyId, Set<String> sampleIds) {
                return List.of();
              }
            });

    boolean exported = exporter.exportData(factory, exportDetails);

    assertFalse("No case lists should be exported", exported);
    assertTrue("No files should be created", factory.getFileContents().isEmpty());
  }

  @Test
  public void testExportCaseLists() {
    var factory = new InMemoryFileWriterFactory();
    CaseListsExporter exporter = new CaseListsExporter(caseListMetadataService);

    boolean exported = exporter.exportData(factory, exportDetails);

    var fileContents = factory.getFileContents();
    assertTrue("Case lists should be exported", exported);
    assertEquals(2, fileContents.size());
    assertTrue(fileContents.containsKey("case_lists/cases_stable_id_1.txt"));
    assertTrue(fileContents.containsKey("case_lists/cases_stable_id_2.txt"));

    assertEquals(
        """
    cancer_study_identifier: STUDY_ID
    stable_id: STUDY_ID_stable_id_1
    case_list_name: Case List 1
    case_list_description: Description for Case List 1
    case_list_ids: SAMPLE_1\tSAMPLE_2
    """,
        fileContents
            .get("case_lists/cases_stable_id_1.txt")
            .toString()); // note: the study id is excluded from the stable id in the file name

    assertEquals(
        """
    cancer_study_identifier: STUDY_ID
    stable_id: STUDY_ID_stable_id_2
    case_list_name: Case List 2
    case_list_description: Description for Case List 2
    case_list_ids: SAMPLE_3\tSAMPLE_4
    """,
        fileContents.get("case_lists/cases_stable_id_2.txt").toString());
  }

  @Test
  public void testExportCaseListsUnderDifferentStudyId() {
    var factory = new InMemoryFileWriterFactory();
    CaseListsExporter exporter = new CaseListsExporter(caseListMetadataService);

    boolean exported =
        exporter.exportData(factory, new ExportDetails(exportDetails.getStudyId(), "STUDY_ID_B"));

    var fileContents = factory.getFileContents();
    assertTrue("Case lists should be exported", exported);
    assertEquals(2, fileContents.size());
    assertTrue(fileContents.containsKey("case_lists/cases_stable_id_1.txt"));
    assertTrue(fileContents.containsKey("case_lists/cases_stable_id_2.txt"));

    assertEquals(
        """
    cancer_study_identifier: STUDY_ID_B
    stable_id: STUDY_ID_B_stable_id_1
    case_list_name: Case List 1
    case_list_description: Description for Case List 1
    case_list_ids: SAMPLE_1\tSAMPLE_2
    """,
        fileContents
            .get("case_lists/cases_stable_id_1.txt")
            .toString()); // note: the study id is excluded from the stable id in the file name

    assertEquals(
        """
    cancer_study_identifier: STUDY_ID_B
    stable_id: STUDY_ID_B_stable_id_2
    case_list_name: Case List 2
    case_list_description: Description for Case List 2
    case_list_ids: SAMPLE_3\tSAMPLE_4
    """,
        fileContents.get("case_lists/cases_stable_id_2.txt").toString());
  }
}
