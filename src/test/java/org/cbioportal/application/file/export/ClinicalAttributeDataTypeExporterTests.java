package org.cbioportal.application.file.export;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Set;
import org.cbioportal.application.file.export.exporters.ClinicalPatientAttributesDataTypeExporter;
import org.cbioportal.application.file.export.exporters.ClinicalSampleAttributesDataTypeExporter;
import org.cbioportal.application.file.export.exporters.ExportDetails;
import org.cbioportal.application.file.export.services.ClinicalAttributeDataService;
import org.cbioportal.application.file.model.ClinicalAttribute;
import org.cbioportal.application.file.model.ClinicalAttributeValue;
import org.cbioportal.application.file.utils.CloseableIterator;
import org.junit.Test;

public class ClinicalAttributeDataTypeExporterTests {

  @Test
  public void testNoClinicalSampleAttributeData() {
    var factory = new InMemoryFileWriterFactory();

    boolean exported =
        new ClinicalSampleAttributesDataTypeExporter(
                new ClinicalAttributeDataService(null) {
                  @Override
                  public boolean hasClinicalSampleAttributes(
                      String studyId, Set<String> sampleIds) {
                    return false;
                  }
                })
            .exportData(factory, new ExportDetails("TEST_STUDY_ID"));

    assertFalse("No data should be exported", exported);
    var fileContents = factory.getFileContents();
    assertTrue(fileContents.isEmpty());
  }

  @Test
  public void testGetClinicalSampleAttributeData() {
    var factory = new InMemoryFileWriterFactory();

    boolean exported =
        new ClinicalSampleAttributesDataTypeExporter(clinicalDataAttributeDataService)
            .exportData(factory, new ExportDetails("TEST_STUDY_ID"));

    assertTrue("Data should be exported", exported);
    var fileContents = factory.getFileContents();
    assertEquals(
        Set.of("meta_clinical_sample_attributes.txt", "data_clinical_sample_attributes.txt"),
        fileContents.keySet());

    assertEquals(
        """
    cancer_study_identifier: TEST_STUDY_ID
    genetic_alteration_type: CLINICAL
    datatype: SAMPLE_ATTRIBUTES
    data_filename: data_clinical_sample_attributes.txt
    """,
        fileContents.get("meta_clinical_sample_attributes.txt").toString());

    assertEquals(
        """
    #Patient Identifier\tSample Identifier\ttest number sample displayName\ttest string sample displayName
    #Patient Identifier\tSample Identifier\ttest number sample description\ttest string sample description
    #STRING\tSTRING\tNUMBER\tSTRING
    #1\t1\t2\t3
    PATIENT_ID\tSAMPLE_ID\tTEST_NUMBER_SAMPLE_ATTRIBUTE_ID\tTEST_STRING_SAMPLE_ATTRIBUTE_ID
    TEST_PATIENT_ID_1\tTEST_SAMPLE_ID_1\t1\tA
    TEST_PATIENT_ID_1\tTEST_SAMPLE_ID_2\t2\t
    TEST_PATIENT_ID_2\tTEST_SAMPLE_ID_3\t\tB
    """,
        fileContents.get("data_clinical_sample_attributes.txt").toString());
  }

  @Test
  public void testGetClinicalSampleAttributeDataUnderAlternativeStudyId() {
    var factory = new InMemoryFileWriterFactory();

    boolean exported =
        new ClinicalSampleAttributesDataTypeExporter(clinicalDataAttributeDataService)
            .exportData(factory, new ExportDetails("TEST_STUDY_ID", "TEST_STUDY_ID_B"));

    assertTrue("Data should be exported", exported);
    var fileContents = factory.getFileContents();
    assertEquals(
        Set.of("meta_clinical_sample_attributes.txt", "data_clinical_sample_attributes.txt"),
        fileContents.keySet());

    assertEquals(
        """
            cancer_study_identifier: TEST_STUDY_ID_B
            genetic_alteration_type: CLINICAL
            datatype: SAMPLE_ATTRIBUTES
            data_filename: data_clinical_sample_attributes.txt
            """,
        fileContents.get("meta_clinical_sample_attributes.txt").toString());
  }

  @Test
  public void testNoClinicalPatientAttributeData() {
    var factory = new InMemoryFileWriterFactory();

    boolean exported =
        new ClinicalPatientAttributesDataTypeExporter(
                new ClinicalAttributeDataService(null) {
                  @Override
                  public boolean hasClinicalPatientAttributes(
                      String studyId, Set<String> sampleIds) {
                    return false;
                  }
                })
            .exportData(factory, new ExportDetails("TEST_STUDY_ID"));

    assertFalse("No data should be exported", exported);
    var fileContents = factory.getFileContents();
    assertTrue(fileContents.isEmpty());
  }

  @Test
  public void testGetClinicalPatientAttributeData() {
    var factory = new InMemoryFileWriterFactory();

    boolean exported =
        new ClinicalPatientAttributesDataTypeExporter(clinicalDataAttributeDataService)
            .exportData(factory, new ExportDetails("TEST_STUDY_ID"));

    assertTrue("Data should be exported", exported);
    var fileContents = factory.getFileContents();
    assertEquals(
        Set.of("meta_clinical_patient_attributes.txt", "data_clinical_patient_attributes.txt"),
        fileContents.keySet());

    assertEquals(
        """
              cancer_study_identifier: TEST_STUDY_ID
              genetic_alteration_type: CLINICAL
              datatype: PATIENT_ATTRIBUTES
              data_filename: data_clinical_patient_attributes.txt
              """,
        fileContents.get("meta_clinical_patient_attributes.txt").toString());

    assertEquals(
        """
             #Patient Identifier\ttest string patient displayName\ttest number patient displayName
             #Patient Identifier\ttest string patient description\ttest number patient description
             #STRING\tSTRING\tNUMBER
             #1\t5\t4
             PATIENT_ID\tTEST_STRING_PATIENT_ATTRIBUTE_ID\tTEST_NUMBER_PATIENT_ATTRIBUTE_ID
             TEST_PATIENT_ID_1\tC\t3
             TEST_PATIENT_ID_2\tD\t
             """,
        fileContents.get("data_clinical_patient_attributes.txt").toString());
  }

  ClinicalAttributeDataService clinicalDataAttributeDataService =
      new ClinicalAttributeDataService(null) {
        @Override
        public boolean hasClinicalSampleAttributes(String studyId, Set<String> sampleIds) {
          return true;
        }

        @Override
        public List<ClinicalAttribute> getClinicalSampleAttributes(String studyId) {
          return List.of(
              new ClinicalAttribute(
                  "test number sample displayName",
                  "test number sample description",
                  "NUMBER",
                  "2",
                  "TEST_NUMBER_SAMPLE_ATTRIBUTE_ID"),
              new ClinicalAttribute(
                  "test string sample displayName",
                  "test string sample description",
                  "STRING",
                  "3",
                  "TEST_STRING_SAMPLE_ATTRIBUTE_ID"));
        }

        @Override
        public boolean hasClinicalPatientAttributes(String studyId, Set<String> sampleIds) {
          return true;
        }

        @Override
        public CloseableIterator<ClinicalAttributeValue> getClinicalSampleAttributeValues(
            String studyId, Set<String> sampleIds) {
          return new SimpleCloseableIterator<>(
              List.of(
                  new ClinicalAttributeValue(1L, "PATIENT_ID", "TEST_PATIENT_ID_1"),
                  new ClinicalAttributeValue(1L, "SAMPLE_ID", "TEST_SAMPLE_ID_1"),
                  new ClinicalAttributeValue(1L, "TEST_NUMBER_SAMPLE_ATTRIBUTE_ID", "1"),
                  new ClinicalAttributeValue(1L, "TEST_STRING_SAMPLE_ATTRIBUTE_ID", "A"),
                  new ClinicalAttributeValue(2L, "PATIENT_ID", "TEST_PATIENT_ID_1"),
                  new ClinicalAttributeValue(2L, "SAMPLE_ID", "TEST_SAMPLE_ID_2"),
                  new ClinicalAttributeValue(2L, "TEST_NUMBER_SAMPLE_ATTRIBUTE_ID", "2"),
                  new ClinicalAttributeValue(3L, "PATIENT_ID", "TEST_PATIENT_ID_2"),
                  new ClinicalAttributeValue(3L, "SAMPLE_ID", "TEST_SAMPLE_ID_3"),
                  new ClinicalAttributeValue(3L, "TEST_STRING_SAMPLE_ATTRIBUTE_ID", "B")));
        }

        @Override
        public List<ClinicalAttribute> getClinicalPatientAttributes(String studyId) {
          return List.of(
              new ClinicalAttribute(
                  "test string patient displayName",
                  "test string patient description",
                  "STRING",
                  "5",
                  "TEST_STRING_PATIENT_ATTRIBUTE_ID"),
              new ClinicalAttribute(
                  "test number patient displayName",
                  "test number patient description",
                  "NUMBER",
                  "4",
                  "TEST_NUMBER_PATIENT_ATTRIBUTE_ID"));
        }

        @Override
        public CloseableIterator<ClinicalAttributeValue> getClinicalPatientAttributeValues(
            String studyId, Set<String> sampleIds) {
          return new SimpleCloseableIterator<>(
              List.of(
                  new ClinicalAttributeValue(1L, "PATIENT_ID", "TEST_PATIENT_ID_1"),
                  new ClinicalAttributeValue(1L, "TEST_STRING_PATIENT_ATTRIBUTE_ID", "C"),
                  new ClinicalAttributeValue(1L, "TEST_NUMBER_PATIENT_ATTRIBUTE_ID", "3"),
                  new ClinicalAttributeValue(2L, "PATIENT_ID", "TEST_PATIENT_ID_2"),
                  new ClinicalAttributeValue(2L, "TEST_STRING_PATIENT_ATTRIBUTE_ID", "D")));
        }
      };
}
