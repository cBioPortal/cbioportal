package org.cbioportal.domain.clinical_data.usecase;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.List;
import org.cbioportal.domain.clinical_data.ClinicalData;
import org.cbioportal.domain.sample.Sample;
import org.cbioportal.domain.sample.usecase.GetFilteredSamplesUseCase;
import org.cbioportal.domain.studyview.StudyViewFilterContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GetClinicalDataForXyPlotUseCaseTest {

  private static final String STUDY_ID = "acc_tcga";
  private static final String PATIENT_ID = "TCGA-OR-A5J1";
  private static final String SAMPLE_ID = "TCGA-OR-A5J1-01";
  private static final String ATTR_AGE = "AGE";
  private static final String ATTR_TUMOR = "TUMOR_SIZE";

  @Mock private GetPatientClinicalDataUseCase getPatientClinicalDataUseCase;
  @Mock private GetSampleClinicalDataUseCase getSampleClinicalDataUseCase;
  @Mock private GetFilteredSamplesUseCase getFilteredSamplesUseCase;
  @Mock private StudyViewFilterContext filterContext;

  private GetClinicalDataForXyPlotUseCase useCase;

  @BeforeEach
  void setUp() {
    useCase =
        new GetClinicalDataForXyPlotUseCase(
            getPatientClinicalDataUseCase, getSampleClinicalDataUseCase, getFilteredSamplesUseCase);
  }

  private Sample sample(int internalId, String sampleId, String patientId, String studyId) {
    return new Sample(
        internalId,
        sampleId,
        "Primary",
        internalId,
        patientId,
        studyId,
        studyId + "_" + sampleId,
        studyId + "_" + patientId);
  }

  private ClinicalData patientData(String patientId, String studyId, String attrId, String value) {
    return new ClinicalData(null, null, patientId, studyId, attrId, value);
  }

  private ClinicalData sampleData(
      String sampleId, String patientId, String studyId, String attrId, String value) {
    return new ClinicalData(1, sampleId, patientId, studyId, attrId, value);
  }

  @Test
  void patientDataIsConvertedToSampleData() {
    ClinicalData patientAge = patientData(PATIENT_ID, STUDY_ID, ATTR_AGE, "65");
    Sample s = sample(1, SAMPLE_ID, PATIENT_ID, STUDY_ID);

    when(getSampleClinicalDataUseCase.execute(filterContext, List.of(ATTR_AGE)))
        .thenReturn(List.of());
    when(getPatientClinicalDataUseCase.execute(filterContext, List.of(ATTR_AGE)))
        .thenReturn(List.of(patientAge));
    when(getFilteredSamplesUseCase.execute(filterContext)).thenReturn(List.of(s));

    List<ClinicalData> result = useCase.execute(filterContext, List.of(ATTR_AGE), false);

    assertEquals(1, result.size());
    assertEquals(SAMPLE_ID, result.get(0).sampleId());
    assertEquals(PATIENT_ID, result.get(0).patientId());
    assertEquals("65", result.get(0).attrValue());
  }

  @Test
  void patientWithMultipleSamplesProducesOneEntryPerSample() {
    ClinicalData patientAge = patientData(PATIENT_ID, STUDY_ID, ATTR_AGE, "65");
    Sample s1 = sample(1, "SAMPLE-01", PATIENT_ID, STUDY_ID);
    Sample s2 = sample(2, "SAMPLE-02", PATIENT_ID, STUDY_ID);

    when(getSampleClinicalDataUseCase.execute(filterContext, List.of(ATTR_AGE)))
        .thenReturn(List.of());
    when(getPatientClinicalDataUseCase.execute(filterContext, List.of(ATTR_AGE)))
        .thenReturn(List.of(patientAge));
    when(getFilteredSamplesUseCase.execute(filterContext)).thenReturn(List.of(s1, s2));

    List<ClinicalData> result = useCase.execute(filterContext, List.of(ATTR_AGE), false);

    assertEquals(2, result.size(), "one entry per sample for patient-level attribute");
    assertTrue(result.stream().anyMatch(d -> "SAMPLE-01".equals(d.sampleId())));
    assertTrue(result.stream().anyMatch(d -> "SAMPLE-02".equals(d.sampleId())));
  }

  @Test
  void patientNotInSampleMap_doesNotThrowNPE() {
    // Previously: patientToSamples.get(patientId) returned null,
    // then chained .get(studyId) caused NullPointerException.
    ClinicalData orphanData = patientData("GHOST-PATIENT", STUDY_ID, ATTR_AGE, "55");
    Sample s = sample(1, SAMPLE_ID, PATIENT_ID, STUDY_ID); // different patient in map

    when(getSampleClinicalDataUseCase.execute(filterContext, List.of(ATTR_AGE)))
        .thenReturn(List.of());
    when(getPatientClinicalDataUseCase.execute(filterContext, List.of(ATTR_AGE)))
        .thenReturn(List.of(orphanData));
    when(getFilteredSamplesUseCase.execute(filterContext)).thenReturn(List.of(s));

    List<ClinicalData> result =
        assertDoesNotThrow(() -> useCase.execute(filterContext, List.of(ATTR_AGE), false));

    assertTrue(result.isEmpty(), "data for patient absent from sample map must be skipped");
  }

  @Test
  void patientInMapButNoSamplesForStudy_doesNotThrowAndSkips() {
    // Patient is in the map (has samples in STUDY_ID) but clinical data references OTHER_STUDY.
    // Previously: samplesByStudy.get(studyId) returned null and the data point was silently lost.
    ClinicalData patientAge = patientData(PATIENT_ID, "OTHER_STUDY", ATTR_AGE, "70");
    Sample s = sample(1, SAMPLE_ID, PATIENT_ID, STUDY_ID);

    when(getSampleClinicalDataUseCase.execute(filterContext, List.of(ATTR_AGE)))
        .thenReturn(List.of());
    when(getPatientClinicalDataUseCase.execute(filterContext, List.of(ATTR_AGE)))
        .thenReturn(List.of(patientAge));
    when(getFilteredSamplesUseCase.execute(filterContext)).thenReturn(List.of(s));

    List<ClinicalData> result =
        assertDoesNotThrow(() -> useCase.execute(filterContext, List.of(ATTR_AGE), false));

    assertTrue(result.isEmpty(), "data point with no matching study samples must be skipped");
  }

  @Test
  void sampleLevelDataIsReturnedUnchanged() {
    ClinicalData sampleTumor = sampleData(SAMPLE_ID, PATIENT_ID, STUDY_ID, ATTR_TUMOR, "3.5");

    when(getSampleClinicalDataUseCase.execute(filterContext, List.of(ATTR_TUMOR)))
        .thenReturn(List.of(sampleTumor));
    when(getPatientClinicalDataUseCase.execute(filterContext, List.of(ATTR_TUMOR)))
        .thenReturn(List.of());

    List<ClinicalData> result = useCase.execute(filterContext, List.of(ATTR_TUMOR), false);

    assertEquals(1, result.size());
    assertEquals("3.5", result.get(0).attrValue());
  }

  @Test
  void emptyValueFilteringRemovesBlankEntries() {
    ClinicalData filled = sampleData(SAMPLE_ID, PATIENT_ID, STUDY_ID, ATTR_TUMOR, "3.5");
    ClinicalData blank = sampleData("S2", PATIENT_ID, STUDY_ID, ATTR_TUMOR, "");

    when(getSampleClinicalDataUseCase.execute(filterContext, List.of(ATTR_TUMOR)))
        .thenReturn(List.of(filled, blank));
    when(getPatientClinicalDataUseCase.execute(filterContext, List.of(ATTR_TUMOR)))
        .thenReturn(List.of());

    List<ClinicalData> result = useCase.execute(filterContext, List.of(ATTR_TUMOR), true);

    assertEquals(1, result.size(), "blank attrValue entries must be filtered out");
    assertEquals("3.5", result.get(0).attrValue());
  }
}
