package org.cbioportal.model;

import java.io.Serializable;
import java.util.Objects;

public class Treatment implements Serializable {
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

    public Integer getStart() {
        return start;
    }

    public void setStart(Integer start) {
        this.start = start;
    }

    public Integer getStop() {
        return stop;
    }

    public void setStop(Integer stop) {
        this.stop = stop;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Treatment)) return false;
        Treatment treatment1 = (Treatment) o;
        return getTreatment().equals(treatment1.getTreatment()) &&
            getStudyId().equals(treatment1.getStudyId()) &&
            getPatientId().equals(treatment1.getPatientId()) &&
            Objects.equals(getStart(), treatment1.getStart()) &&
            Objects.equals(getStop(), treatment1.getStop());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getTreatment(), getStudyId(), getPatientId(), getStart(), getStop());
    }
}
