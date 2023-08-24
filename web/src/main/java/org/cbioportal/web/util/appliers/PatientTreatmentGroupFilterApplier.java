package org.cbioportal.web.util.appliers;

import org.cbioportal.model.ClinicalEventKeyCode;
import org.cbioportal.web.parameter.StudyViewFilter;
import org.cbioportal.web.parameter.filter.AndedPatientTreatmentFilters;
import org.springframework.stereotype.Component;

@Component
public class PatientTreatmentGroupFilterApplier extends AbstractPatientTreatmentFilter {

    @Override
    protected AndedPatientTreatmentFilters getFilters(StudyViewFilter filter) {
        return filter.getPatientTreatmentGroupFilters();
    }

    @Override
    protected ClinicalEventKeyCode getCode() {
        return ClinicalEventKeyCode.AgentClass;
    }
}
