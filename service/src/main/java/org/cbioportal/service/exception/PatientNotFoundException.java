package org.cbioportal.service.exception;

public class PatientNotFoundException extends Exception {

    private String studyId;
    private String patientId;

    public PatientNotFoundException(String studyId, String patientId) {
        super();
        this.studyId = studyId;
        this.patientId = patientId;
    }

    public String getStudyId() {
        return studyId;
    }

    public void setStudyId(String studyId) {
        this.studyId = studyId;
    }

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }
}
