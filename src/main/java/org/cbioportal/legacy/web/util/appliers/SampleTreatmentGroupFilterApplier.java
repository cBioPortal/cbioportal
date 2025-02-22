package org.cbioportal.legacy.web.util.appliers;

import org.cbioportal.legacy.model.ClinicalEventKeyCode;
import org.cbioportal.legacy.web.parameter.StudyViewFilter;
import org.cbioportal.legacy.web.parameter.filter.AndedSampleTreatmentFilters;
import org.springframework.stereotype.Component;

@Component
public class SampleTreatmentGroupFilterApplier extends AbstractSampleTreatmentFilter {
  @Override
  protected AndedSampleTreatmentFilters getFilters(StudyViewFilter filter) {
    return filter.getSampleTreatmentGroupFilters();
  }

  @Override
  protected ClinicalEventKeyCode getCode() {
    return ClinicalEventKeyCode.AgentClass;
  }
}
