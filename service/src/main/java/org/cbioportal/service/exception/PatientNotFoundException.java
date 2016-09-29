package org.cbioportal.service.exception;

public class PatientNotFoundException extends Exception {

    private String patientId;

    public PatientNotFoundException(String patientId) {
        super();
        this.patientId = patientId;
    }

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }
}
