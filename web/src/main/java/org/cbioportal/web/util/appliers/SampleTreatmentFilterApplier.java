package org.cbioportal.web.util.appliers;

import org.cbioportal.model.ClinicalEventKeyCode;
import org.cbioportal.webparam.StudyViewFilter;
import org.cbioportal.webparam.filter.AndedSampleTreatmentFilters;
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
