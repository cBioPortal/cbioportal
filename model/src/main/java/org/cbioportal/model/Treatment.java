package org.cbioportal.model;

public class Treatment {
    private String treatment;
    private String studyId;
    private String patientId;
    private Integer start;
    private Integer stop;

    public String getTreatment() {
        return treatment;
    }

    public void setTreatment(String treatment) {
        this.treatment = treatment;
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

    public int getStart() {
        return start;
    }

    public void setStart(Integer start) {
        this.start = start;
    }

    public int getStop() {
        return stop;
    }

    public void setStop(Integer stop) {
        this.stop = stop;
    }
}
