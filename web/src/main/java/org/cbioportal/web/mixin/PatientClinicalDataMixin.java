package org.cbioportal.web.mixin;

import org.cbioportal.model.ClinicalAttribute;
import org.cbioportal.model.Patient;
import org.cbioportal.web.mixin.summary.ClinicalDataSummaryMixin;

public class PatientClinicalDataMixin extends ClinicalDataSummaryMixin {

    private Patient patient;
    private ClinicalAttribute clinicalAttribute;
}
