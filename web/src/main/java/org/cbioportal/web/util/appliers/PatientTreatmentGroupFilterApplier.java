package org.cbioportal.web.util.appliers;

import org.cbioportal.model.ClinicalEventKeyCode;
import org.cbioportal.webparam.StudyViewFilter;
import org.cbioportal.webparam.filter.AndedPatientTreatmentFilters;
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
