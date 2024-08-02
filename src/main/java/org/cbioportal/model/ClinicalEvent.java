package org.cbioportal.model;

import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Objects;

public class ClinicalEvent extends UniqueKeyBase {
    
    private Integer clinicalEventId;
    @NotNull
    private String studyId;
    @NotNull
    private String patientId;
    @NotNull
    private String eventType;
    private Integer startDate;
    private Integer stopDate;
    private List<ClinicalEventData> attributes;

    public Integer getClinicalEventId() {
        return clinicalEventId;
    }

    public void setClinicalEventId(Integer clinicalEventId) {
        this.clinicalEventId = clinicalEventId;
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

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public Integer getStartDate() {
        return startDate;
    }

    public void setStartDate(Integer startDate) {
        this.startDate = startDate;
    }

    public Integer getStopDate() {
        return stopDate;
    }

    public void setStopDate(Integer stopDate) {
        this.stopDate = stopDate;
    }

    public List<ClinicalEventData> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<ClinicalEventData> attributes) {
        this.attributes = attributes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClinicalEvent that = (ClinicalEvent) o;
        return Objects.equals(clinicalEventId, that.clinicalEventId) && Objects.equals(studyId, that.studyId) && Objects.equals(patientId, that.patientId) && Objects.equals(eventType, that.eventType) && Objects.equals(startDate, that.startDate) && Objects.equals(stopDate, that.stopDate) && Objects.equals(attributes, that.attributes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(clinicalEventId, studyId, patientId, eventType, startDate, stopDate, attributes);
    }
}
