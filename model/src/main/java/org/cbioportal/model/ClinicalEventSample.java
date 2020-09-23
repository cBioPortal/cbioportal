package org.cbioportal.model;

import java.util.Objects;

public class ClinicalEventSample {
    private String patientId;
    private String sampleId;
    private String studyId;
    private Integer timeTaken;

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public String getSampleId() {
        return sampleId;
    }

    public void setSampleId(String sampleId) {
        this.sampleId = sampleId;
    }

    public Integer getTimeTaken() {
        return timeTaken;
    }

    public void setTimeTaken(Integer timeTaken) {
        this.timeTaken = timeTaken;
    }

    public String getStudyId() {
        return studyId;
    }

    public void setStudyId(String studyId) {
        this.studyId = studyId;
    }
    
    public String key() {
        return getSampleId() + getStudyId();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClinicalEventSample that = (ClinicalEventSample) o;
        return getPatientId().equals(that.getPatientId()) &&
            getSampleId().equals(that.getSampleId()) &&
            getStudyId().equals(that.getStudyId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getPatientId(), getSampleId(), getStudyId());
    }
}
