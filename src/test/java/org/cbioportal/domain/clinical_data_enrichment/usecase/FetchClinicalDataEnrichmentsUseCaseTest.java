package org.cbioportal.domain.clinical_data_enrichment.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
