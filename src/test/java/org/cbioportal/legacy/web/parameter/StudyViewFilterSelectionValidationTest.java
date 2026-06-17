package org.cbioportal.legacy.web.parameter;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.List;
import org.cbioportal.application.rest.request.StudyViewFilterDTO;
import org.junit.Before;
import org.junit.Test;

public class StudyViewFilterSelectionValidationTest {

  private Validator validator;

  @Before
  public void setUp() {
    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    validator = factory.getValidator();
  }

  @Test
  public void shouldRejectBlankGenericAssaySelectionFieldsOnStudyViewFilter() {
    StudyViewFilter studyViewFilter = new StudyViewFilter();
    studyViewFilter.setStudyIds(List.of("study_1"));
    studyViewFilter.setGenericAssaySelectionFilters(
        List.of(
            newSelectionFilter(
                " ",
                false,
                List.of(
                    List.of(newSelectionValue("entity_1", "Gain"), newSelectionValue(" ", "%"))))));

    assertFalse(validator.validate(studyViewFilter).isEmpty());
  }

  @Test
  public void shouldRejectBlankGenericAssaySelectionFieldsOnStudyViewFilterDto() {
    StudyViewFilterDTO studyViewFilter = new StudyViewFilterDTO();
    studyViewFilter.setStudyIds(List.of("study_1"));
    studyViewFilter.setGenericAssaySelectionFilters(
        List.of(
            newSelectionFilter(
                "profile",
                true,
                List.of(
                    List.of(newSelectionValue("entity_1", " "), newSelectionValue(" ", "Gain"))))));

    assertFalse(validator.validate(studyViewFilter).isEmpty());
  }

  @Test
  public void shouldAcceptNonBlankGenericAssaySelectionFields() {
    StudyViewFilter studyViewFilter = new StudyViewFilter();
    studyViewFilter.setStudyIds(List.of("study_1"));
    studyViewFilter.setGenericAssaySelectionFilters(
        List.of(
            newSelectionFilter(
                "profile", false, List.of(List.of(newSelectionValue("entity_1", "Gain"))))));

    assertTrue(validator.validate(studyViewFilter).isEmpty());
  }

  private GenericAssaySelectionFilter newSelectionFilter(
      String profileType, boolean patientLevel, List<List<GenericAssaySelectionValue>> values) {
    GenericAssaySelectionFilter filter = new GenericAssaySelectionFilter();
    filter.setProfileType(profileType);
    filter.setPatientLevel(patientLevel);
    filter.setValues(values);
    return filter;
  }

  private GenericAssaySelectionValue newSelectionValue(String stableId, String value) {
    GenericAssaySelectionValue selectionValue = new GenericAssaySelectionValue();
    selectionValue.setStableId(stableId);
    selectionValue.setValue(value);
    return selectionValue;
  }
}
