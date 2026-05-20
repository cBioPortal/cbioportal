package org.cbioportal.domain.clinical_data_enrichment.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import org.cbioportal.domain.clinical_attributes.ClinicalAttribute;
import org.cbioportal.domain.clinical_attributes.repository.ClinicalAttributesRepository;
import org.cbioportal.domain.clinical_data.ClinicalData;
import org.cbioportal.domain.clinical_data.repository.ClinicalDataRepository;
import org.cbioportal.domain.clinical_data_enrichment.ClinicalDataEnrichment;
import org.cbioportal.domain.clinical_data_enrichment.EnrichmentTestMethod;
import org.cbioportal.domain.sample.Sample;
import org.cbioportal.domain.sample.repository.SampleRepository;
import org.cbioportal.legacy.model.ClinicalDataCount;
import org.cbioportal.legacy.model.ClinicalDataCountItem;
import org.cbioportal.legacy.web.parameter.Group;
import org.cbioportal.legacy.web.parameter.GroupFilter;
import org.cbioportal.legacy.web.parameter.SampleIdentifier;
import org.cbioportal.shared.enums.ProjectionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FetchClinicalDataEnrichmentsUseCaseTest {

  @Mock private SampleRepository sampleRepository;
  @Mock private ClinicalDataRepository clinicalDataRepository;
  @Mock private ClinicalAttributesRepository clinicalAttributesRepository;

  private FetchClinicalDataEnrichmentsUseCase useCase;

  private static final String STUDY_ID = "test_study";

  @BeforeEach
  void setUp() {
    useCase =
        new FetchClinicalDataEnrichmentsUseCase(
            sampleRepository, clinicalDataRepository, clinicalAttributesRepository);
  }

  @Test
  void shouldReturnEmptyListWhenNoGroupsProvided() {
    GroupFilter groupFilter = new GroupFilter();
    groupFilter.setGroups(List.of());

    List<ClinicalDataEnrichment> result = useCase.execute(groupFilter);

    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

  @Test
  void shouldReturnEmptyListWhenLessThanTwoGroupsProvided() {
    GroupFilter groupFilter = createGroupFilter(List.of("S1", "S2"), null);

    when(sampleRepository.fetchSamples(
            List.of(STUDY_ID, STUDY_ID), List.of("S1", "S2"), ProjectionType.SUMMARY))
        .thenReturn(createSampleList(List.of("S1", "S2")));

    List<ClinicalDataEnrichment> result = useCase.execute(groupFilter);

    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

  @Test
  void shouldReturnEmptyListWhenAllSamplesInvalid() {
    GroupFilter groupFilter = createGroupFilter(List.of("S1", "S2"), List.of("S3", "S4"));

    when(sampleRepository.fetchSamples(
            List.of(STUDY_ID, STUDY_ID, STUDY_ID, STUDY_ID),
            List.of("S1", "S2", "S3", "S4"),
            ProjectionType.SUMMARY))
        .thenReturn(List.of());

    List<ClinicalDataEnrichment> result = useCase.execute(groupFilter);

    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

  @Test
  void shouldReturnEnrichmentsForCategoricalAttribute() {
    GroupFilter groupFilter = createGroupFilter(List.of("S1", "S2"), List.of("S3", "S4"));

    when(sampleRepository.fetchSamples(
            List.of(STUDY_ID, STUDY_ID, STUDY_ID, STUDY_ID),
            List.of("S1", "S2", "S3", "S4"),
            ProjectionType.SUMMARY))
        .thenReturn(createSampleList(List.of("S1", "S2", "S3", "S4")));

    when(clinicalAttributesRepository.getClinicalAttributesForStudiesDetailed(List.of(STUDY_ID)))
        .thenReturn(List.of(createClinicalAttribute("SEX", "STRING", false)));

    ClinicalDataCountItem countGroup1 = new ClinicalDataCountItem();
    countGroup1.setAttributeId("SEX");
    countGroup1.setCounts(
        Arrays.asList(createClinicalDataCount("Male", 1), createClinicalDataCount("Female", 1)));

    ClinicalDataCountItem countGroup2 = new ClinicalDataCountItem();
    countGroup2.setAttributeId("SEX");
    countGroup2.setCounts(
        Arrays.asList(createClinicalDataCount("Male", 2), createClinicalDataCount("Female", 0)));

    // Mock for first group (sample-level attribute)
    when(clinicalDataRepository.getClinicalDataCountsForEnrichments(
            List.of("test_study_S1", "test_study_S2"),
            List.of("test_study_P1", "test_study_P2"),
            List.of("SEX"),
            List.of(),
            List.of()))
        .thenReturn(List.of(countGroup1));

    // Mock for second group
    when(clinicalDataRepository.getClinicalDataCountsForEnrichments(
            List.of("test_study_S3", "test_study_S4"),
            List.of("test_study_P3", "test_study_P4"),
            List.of("SEX"),
            List.of(),
            List.of()))
        .thenReturn(List.of(countGroup2));

    List<ClinicalDataEnrichment> result = useCase.execute(groupFilter);

    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals("SEX", result.getFirst().clinicalAttribute().attrId());
    assertEquals(EnrichmentTestMethod.CHI_SQUARED, result.getFirst().method());
    assertNotNull(result.getFirst().pValue());
    assertNotNull(result.getFirst().score());
  }

  @Test
  void shouldReturnEnrichmentsForNumericalAttribute() {
    GroupFilter groupFilter = createGroupFilter(List.of("S1", "S2"), List.of("S3", "S4"));

    when(sampleRepository.fetchSamples(
            List.of(STUDY_ID, STUDY_ID, STUDY_ID, STUDY_ID),
            List.of("S1", "S2", "S3", "S4"),
            ProjectionType.SUMMARY))
        .thenReturn(createSampleList(List.of("S1", "S2", "S3", "S4")));

    when(clinicalAttributesRepository.getClinicalAttributesForStudiesDetailed(List.of(STUDY_ID)))
        .thenReturn(List.of(createClinicalAttribute("AGE", "NUMBER", true)));

    // Mock BATCH query using the new optimized method
    // Note: Even for patient-level attributes, both sample and patient IDs are passed
    when(clinicalDataRepository.fetchClinicalDataSummaryForEnrichments(
            List.of(
                "test_study_S1",
                "test_study_S2",
                "test_study_S3",
                "test_study_S4"), // sampleUniqueIds
            List.of(
                "test_study_P1",
                "test_study_P2",
                "test_study_P3",
                "test_study_P4"), // patientUniqueIds
            List.of(), // sampleAttributeIds (empty for patient-level attribute)
            List.of("AGE"), // patientAttributeIds
            List.of())) // conflictingAttributeIds
        .thenReturn(
            Arrays.asList(
                createClinicalData("P1", "AGE", "50"),
                createClinicalData("P2", "AGE", "55"),
                createClinicalData("P3", "AGE", "60"),
                createClinicalData("P4", "AGE", "65")));

    List<ClinicalDataEnrichment> result = useCase.execute(groupFilter);

    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals("AGE", result.getFirst().clinicalAttribute().attrId());
    assertEquals(EnrichmentTestMethod.WILCOXON, result.getFirst().method());
    assertNotNull(result.getFirst().pValue());
    assertTrue(
        result.getFirst().pValue().compareTo(BigDecimal.ZERO) >= 0
            && result.getFirst().pValue().compareTo(BigDecimal.ONE) <= 0);
  }

  @Test
  void shouldUseDifferentTestMethodsBasedOnGroupCount() {
    // Two groups should use Wilcoxon test
    GroupFilter twoGroupFilter = createGroupFilter(List.of("S1", "S2"), List.of("S3", "S4"));

    when(sampleRepository.fetchSamples(
            List.of(STUDY_ID, STUDY_ID, STUDY_ID, STUDY_ID),
            List.of("S1", "S2", "S3", "S4"),
            ProjectionType.SUMMARY))
        .thenReturn(createSampleList(List.of("S1", "S2", "S3", "S4")));

    when(clinicalAttributesRepository.getClinicalAttributesForStudiesDetailed(List.of(STUDY_ID)))
        .thenReturn(List.of(createClinicalAttribute("AGE", "NUMBER", false)));

    // Mock BATCH query using the new optimized method (sample-level attribute)
    // Note: Even for sample-level attributes, both sample and patient IDs are passed
    when(clinicalDataRepository.fetchClinicalDataSummaryForEnrichments(
            List.of(
                "test_study_S1",
                "test_study_S2",
                "test_study_S3",
                "test_study_S4"), // sampleUniqueIds
            List.of(
                "test_study_P1",
                "test_study_P2",
                "test_study_P3",
                "test_study_P4"), // patientUniqueIds
            List.of("AGE"), // sampleAttributeIds
            List.of(), // patientAttributeIds (empty for sample-level attribute)
            List.of())) // conflictingAttributeIds
        .thenReturn(
            Arrays.asList(
                createClinicalData("S1", "AGE", "50"),
                createClinicalData("S2", "AGE", "55"),
                createClinicalData("S3", "AGE", "60"),
                createClinicalData("S4", "AGE", "65")));

    List<ClinicalDataEnrichment> twoGroupResult = useCase.execute(twoGroupFilter);

    assertEquals(1, twoGroupResult.size());
    assertEquals(EnrichmentTestMethod.WILCOXON, twoGroupResult.getFirst().method());

    // Three groups should use Kruskal-Wallis test
    GroupFilter threeGroupFilter = new GroupFilter();
    Group group1 = new Group();
    group1.setName("G1");
    group1.setSampleIdentifiers(createSampleIdentifierList(List.of("S1", "S2")));

    Group group2 = new Group();
    group2.setName("G2");
    group2.setSampleIdentifiers(createSampleIdentifierList(List.of("S3", "S4")));

    Group group3 = new Group();
    group3.setName("G3");
    group3.setSampleIdentifiers(createSampleIdentifierList(List.of("S5", "S6")));

    threeGroupFilter.setGroups(Arrays.asList(group1, group2, group3));

    when(sampleRepository.fetchSamples(
            List.of(STUDY_ID, STUDY_ID, STUDY_ID, STUDY_ID, STUDY_ID, STUDY_ID),
            List.of("S1", "S2", "S3", "S4", "S5", "S6"),
            ProjectionType.SUMMARY))
        .thenReturn(createSampleList(List.of("S1", "S2", "S3", "S4", "S5", "S6")));

    // Mock BATCH query using the new optimized method for all three groups
    // Note: Even for sample-level attributes, both sample and patient IDs are passed
    when(clinicalDataRepository.fetchClinicalDataSummaryForEnrichments(
            List.of(
                "test_study_S1",
                "test_study_S2",
                "test_study_S3",
                "test_study_S4",
                "test_study_S5",
                "test_study_S6"), // sampleUniqueIds
            List.of(
                "test_study_P1",
                "test_study_P2",
                "test_study_P3",
                "test_study_P4",
                "test_study_P5",
                "test_study_P6"), // patientUniqueIds
            List.of("AGE"), // sampleAttributeIds
            List.of(), // patientAttributeIds (empty for sample-level attribute)
            List.of())) // conflictingAttributeIds
        .thenReturn(
            Arrays.asList(
                createClinicalData("S1", "AGE", "50"),
                createClinicalData("S2", "AGE", "55"),
                createClinicalData("S3", "AGE", "60"),
                createClinicalData("S4", "AGE", "65"),
                createClinicalData("S5", "AGE", "70"),
                createClinicalData("S6", "AGE", "75")));

    List<ClinicalDataEnrichment> threeGroupResult = useCase.execute(threeGroupFilter);

    assertEquals(1, threeGroupResult.size());
    assertEquals(EnrichmentTestMethod.KRUSKAL_WALLIS, threeGroupResult.getFirst().method());
  }

  @Test
  void shouldHandleMixedSampleAndPatientAttributes() {
    GroupFilter groupFilter = createGroupFilter(List.of("S1", "S2"), List.of("S3", "S4"));

    when(sampleRepository.fetchSamples(
            List.of(STUDY_ID, STUDY_ID, STUDY_ID, STUDY_ID),
            List.of("S1", "S2", "S3", "S4"),
            ProjectionType.SUMMARY))
        .thenReturn(createSampleList(List.of("S1", "S2", "S3", "S4")));

    when(clinicalAttributesRepository.getClinicalAttributesForStudiesDetailed(List.of(STUDY_ID)))
        .thenReturn(
            Arrays.asList(
                createClinicalAttribute("TUMOR_PURITY", "NUMBER", false),
                createClinicalAttribute("AGE", "NUMBER", true)));

    // Mock BATCH query using the new optimized method (both sample and patient attributes)
    when(clinicalDataRepository.fetchClinicalDataSummaryForEnrichments(
            List.of(
                "test_study_S1",
                "test_study_S2",
                "test_study_S3",
                "test_study_S4"), // sampleUniqueIds
            List.of(
                "test_study_P1",
                "test_study_P2",
                "test_study_P3",
                "test_study_P4"), // patientUniqueIds
            List.of("TUMOR_PURITY"), // sampleAttributeIds
            List.of("AGE"), // patientAttributeIds
            List.of())) // conflictingAttributeIds
        .thenReturn(
            Arrays.asList(
                createClinicalData("S1", "TUMOR_PURITY", "0.8"),
                createClinicalData("S2", "TUMOR_PURITY", "0.9"),
                createClinicalData("S3", "TUMOR_PURITY", "0.7"),
                createClinicalData("S4", "TUMOR_PURITY", "0.6"),
                createClinicalData("P1", "AGE", "50"),
                createClinicalData("P2", "AGE", "55"),
                createClinicalData("P3", "AGE", "60"),
                createClinicalData("P4", "AGE", "65")));

    List<ClinicalDataEnrichment> result = useCase.execute(groupFilter);

    assertEquals(2, result.size());

    boolean hasSampleLevel =
        result.stream().anyMatch(e -> !e.clinicalAttribute().patientAttribute());
    boolean hasPatientLevel =
        result.stream().anyMatch(e -> e.clinicalAttribute().patientAttribute());

    assertTrue(hasSampleLevel);
    assertTrue(hasPatientLevel);
  }

  @Test
  void shouldHandleMixedNumericalAndCategoricalAttributes() {
    GroupFilter groupFilter = createGroupFilter(List.of("S1", "S2"), List.of("S3", "S4"));

    when(sampleRepository.fetchSamples(
            List.of(STUDY_ID, STUDY_ID, STUDY_ID, STUDY_ID),
            List.of("S1", "S2", "S3", "S4"),
            ProjectionType.SUMMARY))
        .thenReturn(createSampleList(List.of("S1", "S2", "S3", "S4")));

    when(clinicalAttributesRepository.getClinicalAttributesForStudiesDetailed(List.of(STUDY_ID)))
        .thenReturn(
            Arrays.asList(
                createClinicalAttribute("AGE", "NUMBER", true),
                createClinicalAttribute("SEX", "STRING", true)));

    // Mock BATCH query using the new optimized method for numerical data (AGE)
    // Note: Even for patient-level attributes, both sample and patient IDs are passed
    when(clinicalDataRepository.fetchClinicalDataSummaryForEnrichments(
            List.of(
                "test_study_S1",
                "test_study_S2",
                "test_study_S3",
                "test_study_S4"), // sampleUniqueIds
            List.of(
                "test_study_P1",
                "test_study_P2",
                "test_study_P3",
                "test_study_P4"), // patientUniqueIds
            List.of(), // sampleAttributeIds (empty for patient-level attribute)
            List.of("AGE"), // patientAttributeIds
            List.of())) // conflictingAttributeIds
        .thenReturn(
            Arrays.asList(
                createClinicalData("P1", "AGE", "50"),
                createClinicalData("P2", "AGE", "55"),
                createClinicalData("P3", "AGE", "60"),
                createClinicalData("P4", "AGE", "65")));

    // Mock categorical data for SEX (group 1)
    ClinicalDataCountItem countGroup1 = new ClinicalDataCountItem();
    countGroup1.setAttributeId("SEX");
    countGroup1.setCounts(List.of(createClinicalDataCount("Male", 2)));

    // Mock categorical data for SEX (group 2)
    ClinicalDataCountItem countGroup2 = new ClinicalDataCountItem();
    countGroup2.setAttributeId("SEX");
    countGroup2.setCounts(
        Arrays.asList(createClinicalDataCount("Male", 1), createClinicalDataCount("Female", 1)));

    when(clinicalDataRepository.getClinicalDataCountsForEnrichments(
            List.of("test_study_S1", "test_study_S2"),
            List.of("test_study_P1", "test_study_P2"),
            List.of(),
            List.of("SEX"),
            List.of()))
        .thenReturn(List.of(countGroup1));

    when(clinicalDataRepository.getClinicalDataCountsForEnrichments(
            List.of("test_study_S3", "test_study_S4"),
            List.of("test_study_P3", "test_study_P4"),
            List.of(),
            List.of("SEX"),
            List.of()))
        .thenReturn(List.of(countGroup2));

    List<ClinicalDataEnrichment> result = useCase.execute(groupFilter);

    assertEquals(2, result.size());

    boolean hasNumerical =
        result.stream()
            .anyMatch(
                e ->
                    e.method() == EnrichmentTestMethod.WILCOXON
                        || e.method() == EnrichmentTestMethod.KRUSKAL_WALLIS);
    boolean hasCategorical =
        result.stream().anyMatch(e -> e.method() == EnrichmentTestMethod.CHI_SQUARED);

    assertTrue(hasNumerical);
    assertTrue(hasCategorical);
  }

  // ==================================================================================
  // Tests for conflicting patient/sample-level attributes across studies
  // ==================================================================================

  @Test
  void shouldHandleConflictingCategoricalAttributeAcrossStudies_TumorStage() {
    // REAL-WORLD CONFLICT VERIFIED VIA cBIOPORTAL API:
    //
    //   nsclc_tracerx_2017 (TRAcking Non-small-cell lung CanceR Evolution through Therapy, Rx):
    //     TUMOR_STAGE -> patientAttribute=true, datatype=STRING
    //
    //   acbc_mskcc_2015 (Adenoid Cystic Breast Carcinoma, MSK 2015):
    //     TUMOR_STAGE -> patientAttribute=false, datatype=STRING
    //
    // The same attribute ID "TUMOR_STAGE" is modeled at patient level in one study and sample
    // level in another. This is a known curation inconsistency in cBioPortal where different
    // studies follow different conventions for the same clinical concept.
    //
    // WHAT THE FIX DOES (commit af3b1505):
    //   ClinicalAttributeUtil.categorizeClinicalAttributes detects that TUMOR_STAGE has
    //   patientAttribute=true in one study and patientAttribute=false in another. It therefore
    //   adds TUMOR_STAGE ONLY to:
    //     - conflictingAttributeIds (handled via the patient-JOIN-sample UNION branch)
    //
    //   The ClickHouse conflicting branch uses a LEFT JOIN so it retrieves data from both the
    //   patient-level study (nsclc_tracerx_2017) and the sample-level study (acbc_mskcc_2015).
    //   sampleAttributeIds is left empty for conflicting attrs to avoid double-counting.

    String studyTracerx = "nsclc_tracerx_2017";
    String studyAcbc = "acbc_mskcc_2015";

    // Group 1: samples from nsclc_tracerx_2017, where TUMOR_STAGE is PATIENT-level.
    // Group 2: samples from acbc_mskcc_2015, where TUMOR_STAGE is SAMPLE-level.
    // The conflict is triggered because both studies are included in the overall query.
    GroupFilter groupFilter = new GroupFilter();
    Group group1 = new Group();
    group1.setName("Group1");
    group1.setSampleIdentifiers(
        Arrays.asList(
            createSampleIdentifier(studyTracerx, "CRUK0001-Tumor1"),
            createSampleIdentifier(studyTracerx, "CRUK0002-Tumor1")));
    Group group2 = new Group();
    group2.setName("Group2");
    group2.setSampleIdentifiers(
        Arrays.asList(
            createSampleIdentifier(studyAcbc, "P-0000001-T01-IM3"),
            createSampleIdentifier(studyAcbc, "P-0000002-T01-IM3")));
    groupFilter.setGroups(Arrays.asList(group1, group2));

    Sample tracerxS1 =
        new Sample(
            1,
            "CRUK0001-Tumor1",
            "Primary",
            1,
            "CRUK0001",
            studyTracerx,
            studyTracerx + "_CRUK0001-Tumor1",
            studyTracerx + "_CRUK0001");
    Sample tracerxS2 =
        new Sample(
            2,
            "CRUK0002-Tumor1",
            "Primary",
            2,
            "CRUK0002",
            studyTracerx,
            studyTracerx + "_CRUK0002-Tumor1",
            studyTracerx + "_CRUK0002");
    Sample acbcS1 =
        new Sample(
            3,
            "P-0000001-T01-IM3",
            "Primary",
            3,
            "P-0000001",
            studyAcbc,
            studyAcbc + "_P-0000001-T01-IM3",
            studyAcbc + "_P-0000001");
    Sample acbcS2 =
        new Sample(
            4,
            "P-0000002-T01-IM3",
            "Primary",
            4,
            "P-0000002",
            studyAcbc,
            studyAcbc + "_P-0000002-T01-IM3",
            studyAcbc + "_P-0000002");

    when(sampleRepository.fetchSamples(
            Arrays.asList(studyTracerx, studyTracerx, studyAcbc, studyAcbc),
            Arrays.asList(
                "CRUK0001-Tumor1", "CRUK0002-Tumor1", "P-0000001-T01-IM3", "P-0000002-T01-IM3"),
            ProjectionType.SUMMARY))
        .thenReturn(Arrays.asList(tracerxS1, tracerxS2, acbcS1, acbcS2));

    // Both studies are discovered. getClinicalAttributesForStudiesDetailed returns the same
    // attribute ID at different levels — the raw per-study metadata before deduplication.
    when(clinicalAttributesRepository.getClinicalAttributesForStudiesDetailed(
            Arrays.asList(studyTracerx, studyAcbc)))
        .thenReturn(
            Arrays.asList(
                // nsclc_tracerx_2017 stores TUMOR_STAGE as a PATIENT attribute
                new ClinicalAttribute(
                    "TUMOR_STAGE",
                    "Tumor Stage",
                    "Tumor Stage",
                    "STRING",
                    true,
                    "1",
                    1,
                    studyTracerx),
                // acbc_mskcc_2015 stores TUMOR_STAGE as a SAMPLE attribute
                new ClinicalAttribute(
                    "TUMOR_STAGE",
                    "Tumor Stage",
                    "Tumor Stage",
                    "STRING",
                    false,
                    "1",
                    1,
                    studyAcbc)));

    // The deduplication step (attrId + patientAttribute) keeps BOTH variants since they differ
    // in patientAttribute. ClinicalAttributeUtil.categorizeClinicalAttributes then detects the
    // conflict and routes TUMOR_STAGE to:
    //   sampleAttributeIds     = []               <- conflicting attrs are NOT added here
    //   conflictingAttributeIds = ["TUMOR_STAGE"]  <- for both studies via the JOIN query
    //
    // The repository call for each group must therefore receive TUMOR_STAGE only in
    // conflictingAttributeIds.

    // Group 1 (tracerx samples): distribution skewed toward early stage (typical in TRACERx
    // which enrolled early-stage lung cancer patients for longitudinal tracking)
    ClinicalDataCountItem group1Counts = new ClinicalDataCountItem();
    group1Counts.setAttributeId("TUMOR_STAGE");
    group1Counts.setCounts(
        Arrays.asList(
            createClinicalDataCount("Stage I", 2), createClinicalDataCount("Stage III", 0)));

    // Group 2 (acbc samples): distribution skewed toward later stage
    ClinicalDataCountItem group2Counts = new ClinicalDataCountItem();
    group2Counts.setAttributeId("TUMOR_STAGE");
    group2Counts.setCounts(
        Arrays.asList(
            createClinicalDataCount("Stage I", 0), createClinicalDataCount("Stage III", 2)));

    // THE KEY ASSERTION IN THE MOCK SETUP:
    // Only conflictingAttributeIds contains "TUMOR_STAGE" — sampleAttributeIds is empty.
    // The conflicting branch in the ClickHouse mapper handles both patient-level and sample-level
    // data via a single LEFT JOIN query, so dual-routing is no longer needed.
    when(clinicalDataRepository.getClinicalDataCountsForEnrichments(
            List.of(
                studyTracerx + "_CRUK0001-Tumor1",
                studyTracerx + "_CRUK0002-Tumor1"), // group 1 sampleUniqueIds
            List.of(
                studyTracerx + "_CRUK0001", studyTracerx + "_CRUK0002"), // group 1 patientUniqueIds
            List.of(), // sampleAttributeIds (empty; conflicting attrs not dual-routed)
            List.of(), // patientAttributeIds (empty; no pure patient-only attrs)
            List.of("TUMOR_STAGE"))) // conflictingAttributeIds <- sole routing for conflicting attr
        .thenReturn(List.of(group1Counts));

    when(clinicalDataRepository.getClinicalDataCountsForEnrichments(
            List.of(
                studyAcbc + "_P-0000001-T01-IM3",
                studyAcbc + "_P-0000002-T01-IM3"), // group 2 sampleUniqueIds
            List.of(studyAcbc + "_P-0000001", studyAcbc + "_P-0000002"), // group 2 patientUniqueIds
            List.of(), // sampleAttributeIds (empty; conflicting attrs not dual-routed)
            List.of(), // patientAttributeIds (empty)
            List.of("TUMOR_STAGE"))) // conflictingAttributeIds <- sole routing for conflicting attr
        .thenReturn(List.of(group2Counts));

    List<ClinicalDataEnrichment> result = useCase.execute(groupFilter);

    assertNotNull(result);
    // Two enrichments are returned: one for the patient-level variant of TUMOR_STAGE
    // (from nsclc_tracerx_2017) and one for the sample-level variant (from acbc_mskcc_2015).
    // Both use the same underlying count data from the combined query, so both produce a
    // chi-squared result reflecting the stage distribution difference between groups.
    assertEquals(2, result.size());
    assertTrue(result.stream().allMatch(e -> "TUMOR_STAGE".equals(e.clinicalAttribute().attrId())));
    assertTrue(result.stream().allMatch(e -> e.method() == EnrichmentTestMethod.CHI_SQUARED));
    // One enrichment per attribute level
    assertTrue(result.stream().anyMatch(e -> e.clinicalAttribute().patientAttribute()));
    assertTrue(result.stream().anyMatch(e -> !e.clinicalAttribute().patientAttribute()));
    // Both should have valid p-values
    assertTrue(result.stream().allMatch(e -> e.pValue() != null));
  }

  @Test
  void shouldHandleConflictingCategoricalAttributeAcrossStudies_TissueSourceSite() {
    // REAL-WORLD CONFLICT VERIFIED VIA cBIOPORTAL API:
    //
    //   thca_tcga (Thyroid Carcinoma, TCGA):
    //     TISSUE_SOURCE_SITE -> patientAttribute=true, datatype=STRING
    //
    //   nsclc_mskcc_2015 (Non-Small Cell Lung Cancer, MSK 2015):
    //     TISSUE_SOURCE_SITE -> patientAttribute=false, datatype=STRING
    //
    // TISSUE_SOURCE_SITE records which tissue bank/site contributed the specimen. TCGA studies
    // (like thca_tcga) typically attach this to the patient record since one patient belongs to
    // one site. MSK studies may attach it to the sample since the same patient could have
    // samples from different sites (e.g., primary and metastatic biopsies from different centres).
    //
    // CONFLICT SCENARIO WITH MIXED GROUPS:
    // Both groups contain samples from BOTH studies. This represents the real enrichment use case
    // where e.g. group 1 = "mutated" samples and group 2 = "wildtype" samples, with both groups
    // drawing from the same multi-study cohort.
    //
    // The fix ensures the mapper queries TISSUE_SOURCE_SITE from:
    //   1. The sample-level table using sampleUniqueIds (covers nsclc_mskcc_2015)
    //   2. The patient-level table joined to samples via sampleUniqueIds (covers thca_tcga)
    // Without the fix (using patientUniqueIds for the conflicting query), data from studies
    // with multi-sample patients could be duplicated or misaligned.

    String studyThca = "thca_tcga";
    String studyNsclc = "nsclc_mskcc_2015";

    // Each group contains samples from both studies to simulate a real cross-study comparison
    GroupFilter groupFilter = new GroupFilter();
    Group group1 = new Group();
    group1.setName("Mutated");
    group1.setSampleIdentifiers(
        Arrays.asList(
            createSampleIdentifier(studyThca, "TCGA-EL-A4K5-01"),
            createSampleIdentifier(studyNsclc, "P-0001234-T01-IM3")));
    Group group2 = new Group();
    group2.setName("Wildtype");
    group2.setSampleIdentifiers(
        Arrays.asList(
            createSampleIdentifier(studyThca, "TCGA-EL-A3T0-01"),
            createSampleIdentifier(studyNsclc, "P-0005678-T01-IM3")));
    groupFilter.setGroups(Arrays.asList(group1, group2));

    Sample thcaS1 =
        new Sample(
            1,
            "TCGA-EL-A4K5-01",
            "Primary",
            1,
            "TCGA-EL-A4K5",
            studyThca,
            studyThca + "_TCGA-EL-A4K5-01",
            studyThca + "_TCGA-EL-A4K5");
    Sample nsclcS1 =
        new Sample(
            2,
            "P-0001234-T01-IM3",
            "Primary",
            2,
            "P-0001234",
            studyNsclc,
            studyNsclc + "_P-0001234-T01-IM3",
            studyNsclc + "_P-0001234");
    Sample thcaS2 =
        new Sample(
            3,
            "TCGA-EL-A3T0-01",
            "Primary",
            3,
            "TCGA-EL-A3T0",
            studyThca,
            studyThca + "_TCGA-EL-A3T0-01",
            studyThca + "_TCGA-EL-A3T0");
    Sample nsclcS2 =
        new Sample(
            4,
            "P-0005678-T01-IM3",
            "Primary",
            4,
            "P-0005678",
            studyNsclc,
            studyNsclc + "_P-0005678-T01-IM3",
            studyNsclc + "_P-0005678");

    when(sampleRepository.fetchSamples(
            Arrays.asList(studyThca, studyNsclc, studyThca, studyNsclc),
            Arrays.asList(
                "TCGA-EL-A4K5-01", "P-0001234-T01-IM3", "TCGA-EL-A3T0-01", "P-0005678-T01-IM3"),
            ProjectionType.SUMMARY))
        .thenReturn(Arrays.asList(thcaS1, nsclcS1, thcaS2, nsclcS2));

    // Raw per-study attribute metadata shows the same attribute at different levels
    when(clinicalAttributesRepository.getClinicalAttributesForStudiesDetailed(
            Arrays.asList(studyThca, studyNsclc)))
        .thenReturn(
            Arrays.asList(
                // thca_tcga: TISSUE_SOURCE_SITE is a PATIENT attribute (one site per patient)
                new ClinicalAttribute(
                    "TISSUE_SOURCE_SITE",
                    "Tissue Source Site",
                    "Tissue Source Site",
                    "STRING",
                    true,
                    "1",
                    1,
                    studyThca),
                // nsclc_mskcc_2015: TISSUE_SOURCE_SITE is a SAMPLE attribute (per-biopsy site)
                new ClinicalAttribute(
                    "TISSUE_SOURCE_SITE",
                    "Tissue Source Site",
                    "Tissue Source Site",
                    "STRING",
                    false,
                    "1",
                    1,
                    studyNsclc)));

    // After conflict detection, TISSUE_SOURCE_SITE lands only in conflictingAttributeIds.
    // The ClickHouse conflicting branch uses a LEFT JOIN covering both the thca_tcga patient
    // records and nsclc_mskcc_2015 sample records. sampleAttributeIds is empty for conflicting
    // attrs to avoid double-counting.

    // Group 1 (Mutated): thyroid sample from site "B3" (TCGA site code) + MSK sample from "MSK"
    ClinicalDataCountItem group1Counts = new ClinicalDataCountItem();
    group1Counts.setAttributeId("TISSUE_SOURCE_SITE");
    group1Counts.setCounts(
        Arrays.asList(createClinicalDataCount("B3", 1), createClinicalDataCount("MSK", 1)));

    // Group 2 (Wildtype): different distribution — both from site "EL" and "MSK" but inverted
    ClinicalDataCountItem group2Counts = new ClinicalDataCountItem();
    group2Counts.setAttributeId("TISSUE_SOURCE_SITE");
    group2Counts.setCounts(
        Arrays.asList(createClinicalDataCount("EL", 1), createClinicalDataCount("MSK", 1)));

    // Group 1 repository call: mixed sampleUniqueIds from both studies
    // TISSUE_SOURCE_SITE is only in conflictingAttributeIds (not dual-routed to sampleAttributeIds)
    when(clinicalDataRepository.getClinicalDataCountsForEnrichments(
            List.of(
                studyThca + "_TCGA-EL-A4K5-01",
                studyNsclc + "_P-0001234-T01-IM3"), // group 1 sampleUniqueIds (mixed studies)
            List.of(
                studyThca + "_TCGA-EL-A4K5",
                studyNsclc + "_P-0001234"), // group 1 patientUniqueIds (mixed studies)
            List.of(), // sampleAttributeIds (empty; conflicting attrs not dual-routed)
            List.of(), // patientAttributeIds (empty)
            List.of("TISSUE_SOURCE_SITE"))) // conflictingAttributeIds <- sole routing
        .thenReturn(List.of(group1Counts));

    // Group 2 repository call: same single-routing for the other group
    when(clinicalDataRepository.getClinicalDataCountsForEnrichments(
            List.of(
                studyThca + "_TCGA-EL-A3T0-01",
                studyNsclc + "_P-0005678-T01-IM3"), // group 2 sampleUniqueIds (mixed studies)
            List.of(
                studyThca + "_TCGA-EL-A3T0",
                studyNsclc + "_P-0005678"), // group 2 patientUniqueIds (mixed studies)
            List.of(), // sampleAttributeIds (empty; conflicting attrs not dual-routed)
            List.of(), // patientAttributeIds (empty)
            List.of("TISSUE_SOURCE_SITE"))) // conflictingAttributeIds <- sole routing
        .thenReturn(List.of(group2Counts));

    List<ClinicalDataEnrichment> result = useCase.execute(groupFilter);

    assertNotNull(result);
    // Two enrichment results: one for the patient-level variant (thca_tcga) and one for the
    // sample-level variant (nsclc_mskcc_2015). Both run the same chi-squared computation
    // since they share the same underlying count data from the dual-routing query.
    assertEquals(2, result.size());
    assertTrue(
        result.stream().allMatch(e -> "TISSUE_SOURCE_SITE".equals(e.clinicalAttribute().attrId())));
    assertTrue(result.stream().allMatch(e -> e.method() == EnrichmentTestMethod.CHI_SQUARED));
    // Verify both attribute levels are represented
    assertTrue(result.stream().anyMatch(e -> e.clinicalAttribute().patientAttribute()));
    assertTrue(result.stream().anyMatch(e -> !e.clinicalAttribute().patientAttribute()));
  }

  @Test
  void shouldRouteConflictingAttributeToSampleIdsNotPatientIds() {
    // REGRESSION TEST: Before the fix in commit af3b1505, conflicting attributes were queried
    // using patientUniqueIds instead of sampleUniqueIds. This caused a subtle bug:
    //
    //   BROKEN behavior (pre-fix):
    //     conflictingAttributeIds query used: patientUniqueIds = ["thca_tcga_TCGA-EL-A4K5"]
    //     → The ClickHouse mapper joined the patient table to samples using patient IDs, but
    //       the IN clause used patient keys, not sample keys. This failed to retrieve data
    //       for sample-level studies, and for multi-sample patients it could double-count.
    //
    //   FIXED behavior (post-fix):
    //     conflictingAttributeIds query uses: sampleUniqueIds = ["thca_tcga_TCGA-EL-A4K5-01"]
    //     → The join is done correctly, matching the sample's patient to the clinical_data row.
    //
    //   CURRENT behavior (single-routing):
    //     TUMOR_STAGE lands ONLY in conflictingAttributeIds (not in sampleAttributeIds).
    //     The conflicting branch's LEFT JOIN covers both patient-level and sample-level data.
    //
    // This test uses ArgumentCaptor to explicitly capture and verify that the repository is
    // called with sampleUniqueIds (not patientUniqueIds) in the conflicting attribute query,
    // and that TUMOR_STAGE is routed only to conflictingAttributeIds (not sampleAttributeIds).
    // It uses the same TUMOR_STAGE conflict: nsclc_tracerx_2017 (patient) vs acbc_mskcc_2015
    // (sample), where the pre-fix behavior would have passed patient keys instead of sample keys.

    String studyTracerx = "nsclc_tracerx_2017";
    String studyAcbc = "acbc_mskcc_2015";

    GroupFilter groupFilter = new GroupFilter();
    Group group1 = new Group();
    group1.setName("Group1");
    group1.setSampleIdentifiers(List.of(createSampleIdentifier(studyTracerx, "CRUK0003-Tumor1")));
    Group group2 = new Group();
    group2.setName("Group2");
    group2.setSampleIdentifiers(List.of(createSampleIdentifier(studyAcbc, "P-0000003-T01-IM3")));
    groupFilter.setGroups(Arrays.asList(group1, group2));

    Sample tracerxS =
        new Sample(
            1,
            "CRUK0003-Tumor1",
            "Primary",
            1,
            "CRUK0003",
            studyTracerx,
            studyTracerx + "_CRUK0003-Tumor1",
            studyTracerx + "_CRUK0003");
    Sample acbcS =
        new Sample(
            2,
            "P-0000003-T01-IM3",
            "Primary",
            2,
            "P-0000003",
            studyAcbc,
            studyAcbc + "_P-0000003-T01-IM3",
            studyAcbc + "_P-0000003");

    when(sampleRepository.fetchSamples(
            Arrays.asList(studyTracerx, studyAcbc),
            Arrays.asList("CRUK0003-Tumor1", "P-0000003-T01-IM3"),
            ProjectionType.SUMMARY))
        .thenReturn(Arrays.asList(tracerxS, acbcS));

    when(clinicalAttributesRepository.getClinicalAttributesForStudiesDetailed(
            Arrays.asList(studyTracerx, studyAcbc)))
        .thenReturn(
            Arrays.asList(
                new ClinicalAttribute(
                    "TUMOR_STAGE",
                    "Tumor Stage",
                    "Tumor Stage",
                    "STRING",
                    true,
                    "1",
                    1,
                    studyTracerx),
                new ClinicalAttribute(
                    "TUMOR_STAGE",
                    "Tumor Stage",
                    "Tumor Stage",
                    "STRING",
                    false,
                    "1",
                    1,
                    studyAcbc)));

    // Use ArgumentCaptor to capture the exact arguments passed to the repository.
    // We want to verify that sampleUniqueIds — not patientUniqueIds — are used as the
    // uniqueIds parameter for the conflicting attribute query.
    @SuppressWarnings("unchecked")
    ArgumentCaptor<List<String>> sampleUniqueIdsCaptor = ArgumentCaptor.forClass(List.class);
    @SuppressWarnings("unchecked")
    ArgumentCaptor<List<String>> patientUniqueIdsCaptor = ArgumentCaptor.forClass(List.class);
    @SuppressWarnings("unchecked")
    ArgumentCaptor<List<String>> sampleAttrIdsCaptor = ArgumentCaptor.forClass(List.class);
    @SuppressWarnings("unchecked")
    ArgumentCaptor<List<String>> patientAttrIdsCaptor = ArgumentCaptor.forClass(List.class);
    @SuppressWarnings("unchecked")
    ArgumentCaptor<List<String>> conflictingAttrIdsCaptor = ArgumentCaptor.forClass(List.class);

    // Return empty counts (sufficient to trigger the repository call without needing enrichments)
    when(clinicalDataRepository.getClinicalDataCountsForEnrichments(
            org.mockito.ArgumentMatchers.anyList(),
            org.mockito.ArgumentMatchers.anyList(),
            org.mockito.ArgumentMatchers.anyList(),
            org.mockito.ArgumentMatchers.anyList(),
            org.mockito.ArgumentMatchers.anyList()))
        .thenReturn(List.of());

    useCase.execute(groupFilter);

    // The repository is called once per group (2 groups → 2 calls). Capture all calls.
    verify(clinicalDataRepository, org.mockito.Mockito.times(2))
        .getClinicalDataCountsForEnrichments(
            sampleUniqueIdsCaptor.capture(),
            patientUniqueIdsCaptor.capture(),
            sampleAttrIdsCaptor.capture(),
            patientAttrIdsCaptor.capture(),
            conflictingAttrIdsCaptor.capture());

    // Verify that TUMOR_STAGE is present only in conflictingAttributeIds, NOT in
    // sampleAttributeIds.
    // The conflicting branch handles both patient-level and sample-level data via a JOIN query,
    // so dual-routing to sampleAttributeIds is no longer needed.
    assertTrue(
        sampleAttrIdsCaptor.getAllValues().stream().noneMatch(list -> list.contains("TUMOR_STAGE")),
        "TUMOR_STAGE must NOT be in sampleAttributeIds — it is handled solely by conflictingAttributeIds");
    assertTrue(
        conflictingAttrIdsCaptor.getAllValues().stream()
            .anyMatch(list -> list.contains("TUMOR_STAGE")),
        "TUMOR_STAGE must be in conflictingAttributeIds so patient-level data is queried via JOIN");

    // Verify that the sampleUniqueIds passed to the repository contain sample keys (not patient
    // keys). Sample keys have the form "<studyId>_<sampleId>" (e.g.
    // "nsclc_tracerx_2017_CRUK0003-Tumor1"),
    // while patient keys have the form "<studyId>_<patientId>" (e.g.
    // "nsclc_tracerx_2017_CRUK0003").
    // Pre-fix code mistakenly passed patient keys for the conflicting attribute query;
    // the fix ensures sample keys are used throughout.
    List<String> capturedSampleIds = sampleUniqueIdsCaptor.getAllValues().get(0);
    assertTrue(
        capturedSampleIds.contains(studyTracerx + "_CRUK0003-Tumor1"),
        "sampleUniqueIds must contain sample key 'nsclc_tracerx_2017_CRUK0003-Tumor1', "
            + "not the patient key 'nsclc_tracerx_2017_CRUK0003'");
  }

  // Helper methods

  private GroupFilter createGroupFilter(List<String> group1Ids, List<String> group2Ids) {
    GroupFilter filter = new GroupFilter();

    Group group1 = new Group();
    group1.setName("Group1");
    group1.setSampleIdentifiers(createSampleIdentifierList(group1Ids));

    if (group2Ids != null) {
      Group group2 = new Group();
      group2.setName("Group2");
      group2.setSampleIdentifiers(createSampleIdentifierList(group2Ids));
      filter.setGroups(Arrays.asList(group1, group2));
    } else {
      filter.setGroups(List.of(group1));
    }

    return filter;
  }

  private List<SampleIdentifier> createSampleIdentifierList(List<String> sampleIds) {
    return sampleIds.stream()
        .map(
            id -> {
              SampleIdentifier identifier = new SampleIdentifier();
              identifier.setStudyId(STUDY_ID);
              identifier.setSampleId(id);
              return identifier;
            })
        .toList();
  }

  /** Creates a SampleIdentifier for a specific study — used in cross-study conflict tests. */
  private SampleIdentifier createSampleIdentifier(String studyId, String sampleId) {
    SampleIdentifier identifier = new SampleIdentifier();
    identifier.setStudyId(studyId);
    identifier.setSampleId(sampleId);
    return identifier;
  }

  private List<Sample> createSampleList(List<String> sampleIds) {
    return sampleIds.stream()
        .map(
            id ->
                new Sample(
                    1,
                    id,
                    "Primary",
                    1,
                    id.replace("S", "P"),
                    STUDY_ID,
                    STUDY_ID + "_" + id,
                    STUDY_ID + "_" + id.replace("S", "P")))
        .toList();
  }

  private ClinicalAttribute createClinicalAttribute(
      String attrId, String datatype, boolean isPatient) {
    return new ClinicalAttribute(
        attrId, attrId + " Name", attrId + " Desc", datatype, isPatient, "1", 1, STUDY_ID);
  }

  private ClinicalData createClinicalData(String entityId, String attrId, String value) {
    // For sample-level data: entityId is sampleId (e.g., "S1")
    // For patient-level data: entityId is patientId (e.g., "P1")
    // We detect by checking if it starts with "P" or "S"
    boolean isPatient = entityId.startsWith("P");
    return new ClinicalData(
        1,
        isPatient ? null : entityId, // sampleId is null for patient-level data
        entityId, // patientId
        STUDY_ID,
        attrId,
        value,
        null);
  }

  private ClinicalDataCount createClinicalDataCount(String value, Integer count) {
    ClinicalDataCount dataCount = new ClinicalDataCount();
    dataCount.setValue(value);
    dataCount.setCount(count);
    return dataCount;
  }
}
