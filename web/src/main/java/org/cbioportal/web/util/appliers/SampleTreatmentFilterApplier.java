package org.cbioportal.web.util.appliers;

import org.cbioportal.model.ClinicalEventKeyCode;
import org.cbioportal.web.parameter.StudyViewFilter;
import org.cbioportal.web.parameter.filter.AndedSampleTreatmentFilters;
import org.springframework.stereotype.Component;

@Component
public class SampleTreatmentFilterApplier extends AbstractSampleTreatmentFilter {

    @Override
    protected AndedSampleTreatmentFilters getFilters(StudyViewFilter filter) {
        return filter.getSampleTreatmentFilters();
    }

    @Override
    protected ClinicalEventKeyCode getCode() {
        return ClinicalEventKeyCode.Agent;
    }
}
