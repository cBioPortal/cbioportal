package org.cbioportal.infrastructure.repository.clickhouse.clinical_data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collections;
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
class ClickHouseClinicalDataRepositoryIntegrationTest extends AbstractClickhouseIntegrationTest {

  private ClickhouseClinicalDataRepository repository;

  @Autowired
  private ClickhouseClinicalDataMapper mapper;

  // Test data based on actual cBioPortal public dataset
  private static final List<String> TEST_STUDY_IDS = Arrays.asList(
      "acc_tcga_pan_can_atlas_2018"
  );
      
  private static final List<String> TEST_SAMPLE_UNIQUE_IDS = Arrays.asList(
      "acc_tcga_pan_can_atlas_2018_TCGA-OR-A5J1-01",
      "acc_tcga_pan_can_atlas_2018_TCGA-OR-A5J2-01",
      "acc_tcga_pan_can_atlas_2018_TCGA-OR-A5J3-01"
  );

  private static final List<String> TEST_PATIENT_UNIQUE_IDS = Arrays.asList(
      "acc_tcga_pan_can_atlas_2018_TCGA-OR-A5J1",
      "acc_tcga_pan_can_atlas_2018_TCGA-OR-A5J2",
      "acc_tcga_pan_can_atlas_2018_TCGA-OR-A5J3"
  );

  private static final List<String> COMMON_SAMPLE_ATTRIBUTES = Arrays.asList(
      "SAMPLE_TYPE",
      "ANEUPLOIDY_SCORE"
  );

  private static final List<String> COMMON_PATIENT_ATTRIBUTES = Arrays.asList(
      "AGE",
      "SEX",
      "PRIOR_DX"
  );

  @BeforeEach
  void setup() {
    repository = new ClickhouseClinicalDataRepository(mapper);
  }

  @Test
  void testFetchClinicalDataId_WithSampleData() {
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
  void testFetchClinicalDataSummary_WithPatientData() {
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

  @Test
  void testFetchClinicalDataDetailed_WithSpecificValues() {
    // When - get detailed data for specific samples we know the values for
    List<ClinicalData> result = repository.fetchClinicalDataDetailed(
        Arrays.asList(
            "acc_tcga_pan_can_atlas_2018_TCGA-OR-A5J1-01",
            "acc_tcga_pan_can_atlas_2018_TCGA-OR-A5J2-01"
        ),
        Arrays.asList("ANEUPLOIDY_SCORE", "SAMPLE_TYPE"),
        TEST_STUDY_IDS,
        ClinicalDataType.SAMPLE
    );

    // Then - verify we get the expected values from real data
    assertFalse(result.isEmpty(), "Should return data for valid sample IDs");

    // Verify specific known values
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

      // Verify DETAILED projection includes clinical attribute metadata
      assertNotNull(data.clinicalAttribute(), "DETAILED projection should include clinical attribute");
      assertEquals(data.attrId(), data.clinicalAttribute().attrId(), "Attribute IDs should match");
    }

    assertTrue(foundJ1Aneuploidy, "Should find ANEUPLOIDY_SCORE for TCGA-OR-A5J1-01");
    assertTrue(foundJ2Aneuploidy, "Should find ANEUPLOIDY_SCORE for TCGA-OR-A5J2-01");
    assertTrue(foundSampleType, "Should find SAMPLE_TYPE data");
  }

  @Test
  void testFetchClinicalDataMeta_WithSampleData() {
    Integer count = repository.fetchClinicalDataMeta(
        TEST_SAMPLE_UNIQUE_IDS,
        COMMON_SAMPLE_ATTRIBUTES,
        TEST_STUDY_IDS,
        ClinicalDataType.SAMPLE
    );

    assertNotNull(count);
    assertEquals(6, count.intValue());

    List<ClinicalData> actualData = repository.fetchClinicalDataSummary(
        TEST_SAMPLE_UNIQUE_IDS,
        COMMON_SAMPLE_ATTRIBUTES,
        TEST_STUDY_IDS,
        ClinicalDataType.SAMPLE
    );
    assertEquals(actualData.size(), count.intValue());
  }

  @Test
  void testFetchClinicalDataId_WithEmptyInput() {
    // When
    List<ClinicalData> result = repository.fetchClinicalDataId(
        List.of(),
        COMMON_SAMPLE_ATTRIBUTES,
        Collections.emptyList(),
        ClinicalDataType.SAMPLE
    );

    // Then
    assertTrue(result.isEmpty(), "Should return empty list for empty input");
  }

  @Test
  void testFetchClinicalDataMeta_WithEmptyInput() {
    // When
    Integer count = repository.fetchClinicalDataMeta(
        List.of(),
        COMMON_SAMPLE_ATTRIBUTES,
        Collections.emptyList(),
        ClinicalDataType.SAMPLE
    );

    // Then
    assertEquals(0, count.intValue(), "Should return 0 count for empty input");
  }

  @Test
  void testProjectionConsistency_SameEntitiesReturned() {
    // When - get data with different projections using subset for focused test
    List<String> testIds = Arrays.asList(
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

    // Then - should return same entities with different levels of detail
    assertEquals(2, idResults.size(), "Should return 2 records for 2 samples with SAMPLE_TYPE");
    assertEquals(idResults.size(), summaryResults.size(),
        "Different projections should return same number of entities");

    // Verify entities match (same internal IDs and attribute IDs)
    for (int i = 0; i < idResults.size(); i++) {
      ClinicalData idData = idResults.get(i);
      ClinicalData summaryData = summaryResults.get(i);

      assertEquals(idData.internalId(), summaryData.internalId(),
          "Internal ID should match across projections");
      assertEquals(idData.attrId(), summaryData.attrId(),
          "Attribute ID should match across projections");
      assertEquals(idData.sampleId(), summaryData.sampleId(),
          "Sample ID should match across projections");

      // Verify projection differences
      assertNull(idData.attrValue(), "ID projection should not have attribute value");
      assertEquals("Primary", summaryData.attrValue(), "SUMMARY projection should have attribute value");
    }
  }

  @Test
  void testClinicalDataType_PatientVsSample() {
    // When
    List<ClinicalData> sampleData = repository.fetchClinicalDataSummary(
        TEST_SAMPLE_UNIQUE_IDS,
        COMMON_SAMPLE_ATTRIBUTES,
        TEST_STUDY_IDS,
        ClinicalDataType.SAMPLE
    );

    List<ClinicalData> patientData = repository.fetchClinicalDataSummary(
        TEST_PATIENT_UNIQUE_IDS,
        COMMON_PATIENT_ATTRIBUTES,
        TEST_STUDY_IDS,
        ClinicalDataType.PATIENT
    );

    // Then - verify correct data type returned
    sampleData.forEach(data -> {
      assertNotNull(data.sampleId(), "Sample data should have sample ID");
      assertNotNull(data.patientId(), "Sample data should also have patient ID");
    });

    patientData.forEach(data -> {
      assertNotNull(data.patientId(), "Patient data should have patient ID");
      // Sample ID may be null or empty for patient-level data
    });
  }
}