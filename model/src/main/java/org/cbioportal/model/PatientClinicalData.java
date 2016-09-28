package org.cbioportal.model;

import org.cbioportal.model.summary.ClinicalDataSummary;

public class PatientClinicalData extends ClinicalDataSummary {

    private Patient patient;
    private ClinicalAttribute clinicalAttribute;

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public ClinicalAttribute getClinicalAttribute() {
        return clinicalAttribute;
    }

    public void setClinicalAttribute(ClinicalAttribute clinicalAttribute) {
        this.clinicalAttribute = clinicalAttribute;
    }
}
