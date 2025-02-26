package org.cbioportal.legacy.web.util.appliers;

import org.cbioportal.legacy.model.ClinicalEventKeyCode;
import org.cbioportal.legacy.web.parameter.StudyViewFilter;
import org.cbioportal.legacy.web.parameter.filter.AndedPatientTreatmentFilters;
import org.springframework.stereotype.Component;

@Component
public class PatientTreatmentFilterApplier extends AbstractPatientTreatmentFilter {

    @Override
    protected AndedPatientTreatmentFilters getFilters(StudyViewFilter filter) {
        return filter.getPatientTreatmentFilters();
    }

    @Override
    protected ClinicalEventKeyCode getCode() {
        return ClinicalEventKeyCode.Agent;
    }
}
