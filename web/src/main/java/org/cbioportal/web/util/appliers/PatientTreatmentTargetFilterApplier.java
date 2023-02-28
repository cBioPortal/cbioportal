package org.cbioportal.web.util.appliers;

import org.cbioportal.model.ClinicalEventKeyCode;
import org.cbioportal.web.parameter.StudyViewFilter;
import org.cbioportal.web.parameter.filter.AndedPatientTreatmentFilters;
import org.springframework.stereotype.Component;

@Component
public class PatientTreatmentTargetFilterApplier extends AbstractPatientTreatmentFilter {

    @Override
    protected AndedPatientTreatmentFilters getFilters(StudyViewFilter filter) {
        return filter.getPatientTreatmentTargetFilters();
    }

    @Override
    protected ClinicalEventKeyCode getCode() {
        return ClinicalEventKeyCode.AgentTarget;
    }
}
