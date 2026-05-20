package org.cbioportal.legacy.web.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Set;
import org.cbioportal.legacy.web.parameter.VirtualStudyData;
import org.cbioportal.legacy.web.parameter.VirtualStudySamples;

public class VirtualStudyDataValidator
    implements ConstraintValidator<ValidVirtualStudyData, VirtualStudyData> {

  @Override
  public boolean isValid(VirtualStudyData value, ConstraintValidatorContext context) {
    if (value == null) {
      return true;
    }

    Set<VirtualStudySamples> studies = value.getStudies();
    if (studies == null || studies.isEmpty()) {
      addViolation(context, "studies", VirtualStudyValidationMessages.STUDIES_REQUIRED);
      return false;
    }

    if (Boolean.TRUE.equals(value.getDynamic())) {
      if (value.getStudyViewFilter() == null) {
        addViolation(
            context, "studyViewFilter", VirtualStudyValidationMessages.DYNAMIC_FILTER_REQUIRED);
        return false;
      }
      return true;
    }

    boolean hasStudyWithoutSamples =
        studies.stream()
            .anyMatch(
                study ->
                    study == null || study.getSamples() == null || study.getSamples().isEmpty());
    if (hasStudyWithoutSamples) {
      addViolation(context, "studies", VirtualStudyValidationMessages.STATIC_SAMPLES_REQUIRED);
      return false;
    }

    return true;
  }

  private void addViolation(ConstraintValidatorContext context, String property, String message) {
    context.disableDefaultConstraintViolation();
    context
        .buildConstraintViolationWithTemplate(message)
        .addPropertyNode(property)
        .addConstraintViolation();
  }
}
