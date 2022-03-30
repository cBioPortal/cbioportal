package org.cbioportal.web.util.appliers;

import org.cbioportal.model.ClinicalEventKeyCode;
import org.cbioportal.web.parameter.StudyViewFilter;
import org.cbioportal.web.parameter.filter.AndedSampleTreatmentFilters;
import org.springframework.stereotype.Component;

@Component
public class SampleTreatmentTargetFilterApplier extends AbstractSampleTreatmentFilter {

    @Override
    protected AndedSampleTreatmentFilters getFilters(StudyViewFilter filter) {
        return filter.getSampleTreatmentTargetFilters();
    }

    @Override
    protected ClinicalEventKeyCode getCode() {
        return ClinicalEventKeyCode.AgentTarget;
    }
}
