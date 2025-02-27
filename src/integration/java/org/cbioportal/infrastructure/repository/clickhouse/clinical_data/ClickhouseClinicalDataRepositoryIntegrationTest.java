package org.cbioportal.infrastructure.repository.clickhouse.clinical_data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
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

  // ID projection tests

  @Test
  void shouldReturnOnlyIdsAndAttributeIdsForIdProjection() {
    List<ClinicalData> result = repository.fetchClinicalDataId(
        TEST_SAMPLE_UNIQUE_IDS,
        COMMON_SAMPLE_ATTRIBUTES,
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
  void shouldIncludeClinicalAttributeMetadataForDetailedProjection() {
    List<ClinicalData> result = repository.fetchClinicalDataDetailed(
        Arrays.asList(
            "acc_tcga_pan_can_atlas_2018_TCGA-OR-A5J1-01",
            "acc_tcga_pan_can_atlas_2018_TCGA-OR-A5J2-01"
        ),
        Arrays.asList("ANEUPLOIDY_SCORE", "SAMPLE_TYPE"),
        ClinicalDataType.SAMPLE
    );

    assertFalse(result.isEmpty());
    result.forEach(data -> {
      assertNotNull(data.clinicalAttribute());
      assertEquals(data.attrId(), data.clinicalAttribute().attrId());
    });
  }

  @Test
  void shouldReturnCorrectAttributeValuesForDetailedProjection() {
    List<ClinicalData> result = repository.fetchClinicalDataDetailed(
        Arrays.asList(
            "acc_tcga_pan_can_atlas_2018_TCGA-OR-A5J1-01",
            "acc_tcga_pan_can_atlas_2018_TCGA-OR-A5J2-01"
        ),
        Arrays.asList("ANEUPLOIDY_SCORE", "SAMPLE_TYPE"),
        ClinicalDataType.SAMPLE
    );

    assertFalse(result.isEmpty());

    boolean foundJ1Aneuploidy = false;
    boolean foundJ2Aneuploidy = false;
    boolean foundSampleType = false;

    for (ClinicalData data : result) {
      if ("TCGA-OR-A5J1-01".equals(data.sampleId()) && "ANEUPLOIDY_SCORE".equals(data.attrId())) {
        assertEquals("2", data.attrValue());
        foundJ1Aneuploidy = true;
      }
      if ("TCGA-OR-A5J2-01".equals(data.sampleId()) && "ANEUPLOIDY_SCORE".equals(data.attrId())) {
        assertEquals("10", data.attrValue());
        foundJ2Aneuploidy = true;
      }
      if ("SAMPLE_TYPE".equals(data.attrId())) {
        assertEquals("Primary", data.attrValue());
        foundSampleType = true;
      }
    }

    assertTrue(foundJ1Aneuploidy);
    assertTrue(foundJ2Aneuploidy);
    assertTrue(foundSampleType);
  }

  // META projection tests

  @Test
  void shouldReturnCorrectCountForMetaProjection() {
    Integer count = repository.fetchClinicalDataMeta(
        TEST_SAMPLE_UNIQUE_IDS,
        COMMON_SAMPLE_ATTRIBUTES,
        ClinicalDataType.SAMPLE
    );

    assertNotNull(count);
    assertEquals(6, count.intValue());

    List<ClinicalData> actualData = repository.fetchClinicalDataSummary(
        TEST_SAMPLE_UNIQUE_IDS,
        COMMON_SAMPLE_ATTRIBUTES,
        ClinicalDataType.SAMPLE
    );
    assertEquals(actualData.size(), count.intValue());
  }

  @Test
  void shouldReturnZeroCountForMetaProjectionWhenNoIdsProvided() {
    Integer count = repository.fetchClinicalDataMeta(
        List.of(),
        COMMON_SAMPLE_ATTRIBUTES,
        ClinicalDataType.SAMPLE
    );

    assertEquals(0, count.intValue());
  }

  // Projection consistency tests

  @Test
  void shouldReturnSameEntitiesAcrossDifferentProjections() {
    List<String> testIds = Arrays.asList(
        "acc_tcga_pan_can_atlas_2018_TCGA-OR-A5J1-01",
        "acc_tcga_pan_can_atlas_2018_TCGA-OR-A5J2-01"
    );
    List<String> testAttrs = List.of("SAMPLE_TYPE");

    List<ClinicalData> idResults = repository.fetchClinicalDataId(
        testIds, testAttrs, ClinicalDataType.SAMPLE
    );

    List<ClinicalData> summaryResults = repository.fetchClinicalDataSummary(
        testIds, testAttrs, ClinicalDataType.SAMPLE
    );

    assertEquals(2, idResults.size());
    assertEquals(idResults.size(), summaryResults.size());

    for (int i = 0; i < idResults.size(); i++) {
      ClinicalData idData = idResults.get(i);
      ClinicalData summaryData = summaryResults.get(i);

      assertEquals(idData.internalId(), summaryData.internalId());
      assertEquals(idData.attrId(), summaryData.attrId());
      assertEquals(idData.sampleId(), summaryData.sampleId());

      assertNull(idData.attrValue());
      assertEquals("Primary", summaryData.attrValue());
    }
  }

  // Clinical data type tests

  @Test
  void shouldReturnSampleIdForSampleTypeClinicalData() {
    List<ClinicalData> sampleData = repository.fetchClinicalDataSummary(
        TEST_SAMPLE_UNIQUE_IDS,
        COMMON_SAMPLE_ATTRIBUTES,
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
        ClinicalDataType.PATIENT
    );

    patientData.forEach(data -> {
      assertNotNull(data.patientId());
    });
  }
}