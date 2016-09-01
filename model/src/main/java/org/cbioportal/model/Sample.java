package org.cbioportal.model;

import org.cbioportal.model.summary.SampleSummary;

public class Sample extends SampleSummary {

    private Patient patient;

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }
}