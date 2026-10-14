package org.cbioportal.domain.clinical_data.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.cbioportal.domain.clinical_attributes.ClinicalAttribute;
import org.cbioportal.domain.clinical_attributes.usecase.GetClinicalAttributesForStudiesUseCase;
import org.cbioportal.domain.clinical_data.repository.ClinicalDataRepository;
import org.cbioportal.domain.patient.usecase.GetFilteredPatientCountUseCase;
import org.cbioportal.domain.sample.usecase.GetFilteredSamplesCountUseCase;
import org.cbioportal.domain.studyview.StudyViewFilterContext;
import org.cbioportal.legacy.model.ClinicalDataCount;
import org.cbioportal.legacy.model.ClinicalDataCountItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GetClinicalDataCountsUseCaseTest {

  private static final String STUDY_1 = "glioma_mskcc_2019";
  private static final String STUDY_2 = "lgg_tcga_pan_can_atlas_2018";
  private static final int FILTERED_SAMPLE_COUNT = 5;
  private static final int FILTERED_PATIENT_COUNT = 3;

  @Mock private ClinicalDataRepository clinicalDataRepository;
  @Mock private GetClinicalAttributesForStudiesUseCase getClinicalAttributesForStudiesUseCase;
  @Mock private GetFilteredSamplesCountUseCase getFilteredSamplesCountUseCase;
  @Mock private GetFilteredPatientCountUseCase getFilteredPatientCountUseCase;
  @Mock private StudyViewFilterContext filterContext;

  private GetClinicalDataCountsUseCase useCase;

  @BeforeEach
  void setUp() {
    useCase =
        new GetClinicalDataCountsUseCase(
            clinicalDataRepository,
            getClinicalAttributesForStudiesUseCase,
            getFilteredSamplesCountUseCase,
            getFilteredPatientCountUseCase);
    when(filterContext.customDataFilterCancerStudies()).thenReturn(List.of(STUDY_1, STUDY_2));
  }

  @Test
  void attributesNotDefinedForAnyStudyAreReturnedAsNaWithoutQueryingCounts() {
    when(getClinicalAttributesForStudiesUseCase.execute(List.of(STUDY_1, STUDY_2)))
        .thenReturn(List.of(attribute("CANCER_TYPE", false, STUDY_1)));
    stubFilteredCounts();

    List<ClinicalDataCountItem> result = useCase.execute(filterContext, List.of("ETHNICITY"));

    verifyNoInteractions(clinicalDataRepository);
    assertEquals(
        Map.of("ETHNICITY", Map.of("NA", FILTERED_SAMPLE_COUNT)), countsByAttribute(result));
  }

  @Test
  void missingAttributeDefinedInSeveralStudiesIsRestoredOnce() {
    when(getClinicalAttributesForStudiesUseCase.execute(List.of(STUDY_1, STUDY_2)))
        .thenReturn(
            List.of(attribute("ETHNICITY", true, STUDY_1), attribute("ETHNICITY", true, STUDY_2)));
    when(clinicalDataRepository.getClinicalDataCounts(any(), anyList(), anyList(), anyList()))
        .thenReturn(List.of());
    stubFilteredCounts();

    List<ClinicalDataCountItem> result = useCase.execute(filterContext, List.of("ETHNICITY"));

    assertEquals(1, result.size());
    assertEquals(
        Map.of("ETHNICITY", Map.of("NA", FILTERED_PATIENT_COUNT)), countsByAttribute(result));
  }

  @Test
  void missingSampleAndConflictingAttributesAreCountedPerSample() {
    when(getClinicalAttributesForStudiesUseCase.execute(List.of(STUDY_1, STUDY_2)))
        .thenReturn(
            List.of(
                attribute("SAMPLE_TYPE", false, STUDY_1),
                attribute("TUMOR_GRADE", false, STUDY_1),
                attribute("TUMOR_GRADE", true, STUDY_2)));
    when(clinicalDataRepository.getClinicalDataCounts(
            filterContext, List.of("SAMPLE_TYPE"), List.of(), List.of("TUMOR_GRADE")))
        .thenReturn(List.of());
    stubFilteredCounts();

    List<ClinicalDataCountItem> result =
        useCase.execute(filterContext, List.of("SAMPLE_TYPE", "TUMOR_GRADE"));

    assertEquals(
        Map.of(
            "SAMPLE_TYPE", Map.of("NA", FILTERED_SAMPLE_COUNT),
            "TUMOR_GRADE", Map.of("NA", FILTERED_SAMPLE_COUNT)),
        countsByAttribute(result));
  }

  @Test
  void undefinedAttributeIsAddedAlongsideReturnedCounts() {
    when(getClinicalAttributesForStudiesUseCase.execute(List.of(STUDY_1, STUDY_2)))
        .thenReturn(List.of(attribute("SEX", true, STUDY_1)));
    when(clinicalDataRepository.getClinicalDataCounts(
            filterContext, List.of(), List.of("SEX"), List.of()))
        .thenReturn(List.of(countItem("SEX", Map.of("Male", 1, "Female", 2))));
    stubFilteredCounts();

    List<ClinicalDataCountItem> result = useCase.execute(filterContext, List.of("SEX", "FAKE"));

    assertEquals(
        Map.of(
            "SEX", Map.of("Male", 1, "Female", 2),
            "FAKE", Map.of("NA", FILTERED_SAMPLE_COUNT)),
        countsByAttribute(result));
  }

  @Test
  void filteredCountsAreNotQueriedWhenNoAttributeIsMissing() {
    when(getClinicalAttributesForStudiesUseCase.execute(List.of(STUDY_1, STUDY_2)))
        .thenReturn(List.of(attribute("SEX", true, STUDY_1)));
    when(clinicalDataRepository.getClinicalDataCounts(
            filterContext, List.of(), List.of("SEX"), List.of()))
        .thenReturn(List.of(countItem("SEX", Map.of("Male", 1))));

    List<ClinicalDataCountItem> result = useCase.execute(filterContext, List.of("SEX"));

    assertEquals(Map.of("SEX", Map.of("Male", 1)), countsByAttribute(result));
    verify(getFilteredSamplesCountUseCase, never()).execute(any());
    verify(getFilteredPatientCountUseCase, never()).execute(any());
  }

  private void stubFilteredCounts() {
    when(getFilteredSamplesCountUseCase.execute(filterContext)).thenReturn(FILTERED_SAMPLE_COUNT);
    when(getFilteredPatientCountUseCase.execute(filterContext)).thenReturn(FILTERED_PATIENT_COUNT);
  }

  private static ClinicalAttribute attribute(
      String attrId, boolean patientAttribute, String studyId) {
    return new ClinicalAttribute(
        attrId, attrId, attrId, "STRING", patientAttribute, "1", null, studyId);
  }

  private static ClinicalDataCountItem countItem(String attributeId, Map<String, Integer> counts) {
    ClinicalDataCountItem item = new ClinicalDataCountItem();
    item.setAttributeId(attributeId);
    item.setCounts(
        counts.entrySet().stream()
            .map(
                entry -> {
                  ClinicalDataCount count = new ClinicalDataCount();
                  count.setAttributeId(attributeId);
                  count.setValue(entry.getKey());
                  count.setCount(entry.getValue());
                  return count;
                })
            .toList());
    return item;
  }

  private static Map<String, Map<String, Integer>> countsByAttribute(
      List<ClinicalDataCountItem> items) {
    return items.stream()
        .collect(
            Collectors.toMap(
                ClinicalDataCountItem::getAttributeId,
                item ->
                    item.getCounts().stream()
                        .collect(
                            Collectors.toMap(
                                ClinicalDataCount::getValue, ClinicalDataCount::getCount))));
  }
}
