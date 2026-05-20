package org.cbioportal.legacy.web.parameter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.List;
import java.util.Set;
import org.cbioportal.legacy.web.validation.VirtualStudyValidationMessages;
import org.junit.Before;
import org.junit.Test;

public class VirtualStudyDataValidationTest {

  private Validator validator;

  @Before
  public void setUp() {
    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    validator = factory.getValidator();
  }

  @Test
  public void shouldRejectBlankName() {
    VirtualStudyData virtualStudyData = createStaticVirtualStudyData();
    virtualStudyData.setName("  ");

    assertSingleViolation(virtualStudyData, "name", VirtualStudyValidationMessages.NAME_REQUIRED);
  }

  @Test
  public void shouldRejectMissingStudies() {
    VirtualStudyData virtualStudyData = createStaticVirtualStudyData();
    virtualStudyData.setStudies(Set.of());

    assertHasViolation(
        virtualStudyData, "studies", VirtualStudyValidationMessages.STUDIES_REQUIRED);
  }

  @Test
  public void shouldRejectStudyWithoutId() {
    VirtualStudyData virtualStudyData = createStaticVirtualStudyData();
    virtualStudyData.getStudies().iterator().next().setId(" ");

    assertSingleViolation(
        virtualStudyData, "studies[].id", VirtualStudyValidationMessages.STUDY_ID_REQUIRED);
  }

  @Test
  public void shouldRejectDynamicVirtualStudyWithoutFilter() {
    VirtualStudyData virtualStudyData = createDynamicVirtualStudyData();
    virtualStudyData.setStudyViewFilter(null);

    assertSingleViolation(
        virtualStudyData,
        "studyViewFilter",
        VirtualStudyValidationMessages.DYNAMIC_FILTER_REQUIRED);
  }

  @Test
  public void shouldRejectStaticVirtualStudyWithoutSamples() {
    VirtualStudyData virtualStudyData = createStaticVirtualStudyData();
    virtualStudyData.getStudies().iterator().next().setSamples(Set.of());

    assertSingleViolation(
        virtualStudyData, "studies", VirtualStudyValidationMessages.STATIC_SAMPLES_REQUIRED);
  }

  @Test
  public void shouldRejectMissingSamplesWhenDynamicFlagIsNull() {
    VirtualStudyData virtualStudyData = createStaticVirtualStudyData();
    virtualStudyData.setDynamic(null);
    virtualStudyData.getStudies().iterator().next().setSamples(Set.of());

    assertSingleViolation(
        virtualStudyData, "studies", VirtualStudyValidationMessages.STATIC_SAMPLES_REQUIRED);
  }

  @Test
  public void shouldAcceptValidStaticVirtualStudy() {
    Set<ConstraintViolation<VirtualStudyData>> violations =
        validator.validate(createStaticVirtualStudyData());

    assertTrue(violations.isEmpty());
  }

  @Test
  public void shouldAcceptValidDynamicVirtualStudy() {
    Set<ConstraintViolation<VirtualStudyData>> violations =
        validator.validate(createDynamicVirtualStudyData());

    assertTrue(violations.isEmpty());
  }

  private void assertSingleViolation(
      VirtualStudyData virtualStudyData, String propertyPath, String message) {
    Set<ConstraintViolation<VirtualStudyData>> violations = validator.validate(virtualStudyData);

    List<String> actualViolations =
        violations.stream()
            .map(violation -> violation.getPropertyPath() + "|" + violation.getMessage())
            .toList();

    assertEquals(1, actualViolations.size());
    assertEquals(propertyPath + "|" + message, actualViolations.getFirst());
  }

  private void assertHasViolation(
      VirtualStudyData virtualStudyData, String propertyPath, String message) {
    Set<ConstraintViolation<VirtualStudyData>> violations = validator.validate(virtualStudyData);

    List<String> actualViolations =
        violations.stream()
            .map(violation -> violation.getPropertyPath() + "|" + violation.getMessage())
            .toList();

    assertTrue(actualViolations.contains(propertyPath + "|" + message));
  }

  private VirtualStudyData createStaticVirtualStudyData() {
    VirtualStudyData virtualStudyData = new VirtualStudyData();
    virtualStudyData.setName("Static virtual study");
    virtualStudyData.setDynamic(false);

    VirtualStudySamples virtualStudySamples = new VirtualStudySamples();
    virtualStudySamples.setId("study_1");
    virtualStudySamples.setSamples(Set.of("sample_1"));
    virtualStudyData.setStudies(Set.of(virtualStudySamples));

    return virtualStudyData;
  }

  private VirtualStudyData createDynamicVirtualStudyData() {
    VirtualStudyData virtualStudyData = new VirtualStudyData();
    virtualStudyData.setName("Dynamic virtual study");
    virtualStudyData.setDynamic(true);

    VirtualStudySamples virtualStudySamples = new VirtualStudySamples();
    virtualStudySamples.setId("study_1");
    virtualStudyData.setStudies(Set.of(virtualStudySamples));

    StudyViewFilter studyViewFilter = new StudyViewFilter();
    studyViewFilter.setStudyIds(List.of("study_1"));
    virtualStudyData.setStudyViewFilter(studyViewFilter);

    return virtualStudyData;
  }
}
