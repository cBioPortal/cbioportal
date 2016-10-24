package org.cbioportal.model.summary;

import org.cbioportal.model.Sample.SampleType;

import java.io.Serializable;

public abstract class SampleSummary implements Serializable {

    private Integer internalId;
    private String stableId;
    private SampleType sampleType;
    private Integer patientId;
    private String patientStableId;
    private String typeOfCancerId;

    public Integer getInternalId() {
        return internalId;
    }

    public void setInternalId(Integer internalId) {
        this.internalId = internalId;
    }

    public String getStableId() {
        return stableId;
    }

    public void setStableId(String stableId) {
        this.stableId = stableId;
    }

    public SampleType getSampleType() {
        return sampleType;
    }

    public void setSampleType(SampleType sampleType) {
        this.sampleType = sampleType;
    }

    public Integer getPatientId() {
        return patientId;
    }

    public void setPatientId(Integer patientId) {
        this.patientId = patientId;
    }

    public String getPatientStableId() {
        return patientStableId;
    }

    public void setPatientStableId(String patientStableId) {
        this.patientStableId = patientStableId;
    }

    public String getTypeOfCancerId() {
        return typeOfCancerId;
    }

    public void setTypeOfCancerId(String typeOfCancerId) {
        this.typeOfCancerId = typeOfCancerId;
    }
}
