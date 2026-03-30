package org.cbioportal.infrastructure.repository.clickhouse.clinical_data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.cbioportal.AbstractClickhouseIntegrationTest;
import org.cbioportal.domain.clinical_data.ClinicalData;
import org.cbioportal.domain.clinical_data.ClinicalDataType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
class ClickhouseClinicalDataRepositoryIntegrationTest extends AbstractClickhouseIntegrationTest {

  private ClickhouseClinicalDataRepository repository;

  @Autowired
  private ClickhouseClinicalDataMapper mapper;

  // Test data based on actual cBioPortal public dataset
  private static final List<String> TEST_STUDY_IDS = List.of(
      "acc_tcga_pan_can_atlas_2018"
  );

  private static final List<String> TEST_SAMPLE_UNIQUE_IDS = List.of(
      "acc_tcga_pan_can_atlas_2018_TCGA-OR-A5J1-01",
      "acc_tcga_pan_can_atlas_2018_TCGA-OR-A5J2-01",
      "acc_tcga_pan_can_atlas_2018_TCGA-OR-A5J3-01"
  );

  private static final List<String> TEST_PATIENT_UNIQUE_IDS = List.of(
      "acc_tcga_pan_can_atlas_2018_TCGA-OR-A5J1",
      "acc_tcga_pan_can_atlas_2018_TCGA-OR-A5J2",
      "acc_tcga_pan_can_atlas_2018_TCGA-OR-A5J3"
  );

  private static final List<String> COMMON_SAMPLE_ATTRIBUTES = List.of(
      "SAMPLE_TYPE",
      "ANEUPLOIDY_SCORE"
  );

  private static final List<String> COMMON_PATIENT_ATTRIBUTES = List.of(
      "AGE",
      "SEX",
      "PRIOR_DX"
  );

  @BeforeEach
  void setup() {
    repository = new ClickhouseClinicalDataRepository(mapper);
  }

  // ID projection tests

  @Test
  void shouldReturnOnlyIdsAndAttributeIdsForIdProjection() {
    List<ClinicalData> result = repository.fetchClinicalDataId(
        TEST_SAMPLE_UNIQUE_IDS,
        COMMON_SAMPLE_ATTRIBUTES,
        TEST_STUDY_IDS,
        ClinicalDataType.SAMPLE
    );

    assertFalse(result.isEmpty());
    result.forEach(clinicalData -> {
      assertNotNull(clinicalData.internalId());
      assertNotNull(clinicalData.sampleId());
      assertNotNull(clinicalData.studyId());
      assertNotNull(clinicalData.attrId());
      assertNull(clinicalData.attrValue());
      assertNull(clinicalData.clinicalAttribute());
    });
  }

  @Test
  void shouldReturnEmptyListForIdProjectionWhenNoIdsProvided() {
    List<ClinicalData> result = repository.fetchClinicalDataId(
        List.of(),
        COMMON_SAMPLE_ATTRIBUTES,
        List.of(),
        ClinicalDataType.SAMPLE
    );

    assertTrue(result.isEmpty());
  }

  // SUMMARY projection tests

  @Test
  void shouldIncludeAttributeValuesForSummaryProjection() {
    List<ClinicalData> result = repository.fetchClinicalDataSummary(
        TEST_PATIENT_UNIQUE_IDS,
        COMMON_PATIENT_ATTRIBUTES,
        TEST_STUDY_IDS,
        ClinicalDataType.PATIENT
    );

    assertFalse(result.isEmpty());
    result.forEach(clinicalData -> {
      assertNotNull(clinicalData.internalId());
      assertNotNull(clinicalData.patientId());
      assertNotNull(clinicalData.studyId());
      assertNotNull(clinicalData.attrId());
      assertNotNull(clinicalData.attrValue());
      assertNull(clinicalData.clinicalAttribute());
    });
  }

  // DETAILED projection tests

  @Test
  void shouldReturnCorrectAttributeValuesForDetailedProjection() {
    List<ClinicalData> result = repository.fetchClinicalDataDetailed(
        List.of(
            "acc_tcga_pan_can_atlas_2018_TCGA-OR-A5J1-01",
            "acc_tcga_pan_can_atlas_2018_TCGA-OR-A5J2-01"
        ),
        List.of("ANEUPLOIDY_SCORE", "SAMPLE_TYPE"),
        TEST_STUDY_IDS,
        ClinicalDataType.SAMPLE
    );

    assertFalse(result.isEmpty(), "Should return data for valid sample IDs");

    boolean foundJ1Aneuploidy = false;
    boolean foundJ2Aneuploidy = false;
    boolean foundSampleType = false;

    for (ClinicalData data : result) {
      if ("TCGA-OR-A5J1-01".equals(data.sampleId()) && "ANEUPLOIDY_SCORE".equals(data.attrId())) {
        assertEquals("2", data.attrValue(), "TCGA-OR-A5J1-01 should have ANEUPLOIDY_SCORE = 2");
        foundJ1Aneuploidy = true;
      }
      if ("TCGA-OR-A5J2-01".equals(data.sampleId()) && "ANEUPLOIDY_SCORE".equals(data.attrId())) {
        assertEquals("10", data.attrValue(), "TCGA-OR-A5J2-01 should have ANEUPLOIDY_SCORE = 10");
        foundJ2Aneuploidy = true;
      }
      if ("SAMPLE_TYPE".equals(data.attrId())) {
        assertEquals("Primary", data.attrValue(), "All samples should have SAMPLE_TYPE = Primary");
        foundSampleType = true;
      }

      assertNotNull(data.clinicalAttribute(), "DETAILED projection should include clinical attribute");
      assertEquals(data.attrId(), data.clinicalAttribute().attrId(), "Attribute IDs should match");
    }

    assertTrue(foundJ1Aneuploidy, "Should find ANEUPLOIDY_SCORE for TCGA-OR-A5J1-01");
    assertTrue(foundJ2Aneuploidy, "Should find ANEUPLOIDY_SCORE for TCGA-OR-A5J2-01");
    assertTrue(foundSampleType, "Should find SAMPLE_TYPE data");
  }

  // META projection tests

  @Test
  void shouldReturnCorrectCountForMetaProjection() {
    Integer count = repository.fetchClinicalDataMeta(
        TEST_SAMPLE_UNIQUE_IDS,
        COMMON_SAMPLE_ATTRIBUTES,
        TEST_STUDY_IDS,
        ClinicalDataType.SAMPLE
    );

    assertNotNull(count);
    assertEquals(6, count);

    List<ClinicalData> actualData = repository.fetchClinicalDataSummary(
        TEST_SAMPLE_UNIQUE_IDS,
        COMMON_SAMPLE_ATTRIBUTES,
        TEST_STUDY_IDS,
        ClinicalDataType.SAMPLE
    );
    assertEquals(actualData.size(), count);
  }

  @Test
  void shouldReturnZeroCountForMetaProjectionWhenNoIdsProvided() {
    Integer count = repository.fetchClinicalDataMeta(
        List.of(),
        COMMON_SAMPLE_ATTRIBUTES,
        List.of(),
        ClinicalDataType.SAMPLE
    );

    assertEquals(0, count, "Should return 0 count for empty input");
  }

  // Projection consistency tests

  @Test
  void shouldReturnSameEntitiesAcrossDifferentProjections() {
    List<String> testIds = List.of(
        "acc_tcga_pan_can_atlas_2018_TCGA-OR-A5J1-01",
        "acc_tcga_pan_can_atlas_2018_TCGA-OR-A5J2-01"
    );
    List<String> testAttrs = List.of("SAMPLE_TYPE");

    List<ClinicalData> idResults = repository.fetchClinicalDataId(
        testIds, testAttrs, TEST_STUDY_IDS, ClinicalDataType.SAMPLE
    );

    List<ClinicalData> summaryResults = repository.fetchClinicalDataSummary(
        testIds, testAttrs, TEST_STUDY_IDS, ClinicalDataType.SAMPLE
    );

    assertEquals(2, idResults.size(), "Should return 2 records for 2 samples with SAMPLE_TYPE");
    assertEquals(idResults.size(), summaryResults.size(),
        "Different projections should return same number of entities");

    for (int i = 0; i < idResults.size(); i++) {
      ClinicalData idData = idResults.get(i);
      ClinicalData summaryData = summaryResults.get(i);

      assertEquals(idData.internalId(), summaryData.internalId(),
          "Internal ID should match across projections");
      assertEquals(idData.attrId(), summaryData.attrId(),
          "Attribute ID should match across projections");
      assertEquals(idData.sampleId(), summaryData.sampleId(),
          "Sample ID should match across projections");

      assertNull(idData.attrValue(), "ID projection should not have attribute value");
      assertEquals("Primary", summaryData.attrValue(), "SUMMARY projection should have attribute value");
    }
  }

  // Clinical data type tests

  @Test
  void shouldReturnSampleIdForSampleTypeClinicalData() {
    List<ClinicalData> sampleData = repository.fetchClinicalDataSummary(
        TEST_SAMPLE_UNIQUE_IDS,
        COMMON_SAMPLE_ATTRIBUTES,
        TEST_STUDY_IDS,
        ClinicalDataType.SAMPLE
    );

    sampleData.forEach(data -> {
      assertNotNull(data.sampleId());
      assertNotNull(data.patientId());
    });
  }

  @Test
  void shouldReturnPatientIdForPatientTypeClinicalData() {
    List<ClinicalData> patientData = repository.fetchClinicalDataSummary(
        TEST_PATIENT_UNIQUE_IDS,
        COMMON_PATIENT_ATTRIBUTES,
        TEST_STUDY_IDS,
        ClinicalDataType.PATIENT
    );

    patientData.forEach(data -> {
      assertNotNull(data.patientId(), "Patient data should have patient ID");
      // Sample ID may be null or empty for patient-level data
    });
  }
}