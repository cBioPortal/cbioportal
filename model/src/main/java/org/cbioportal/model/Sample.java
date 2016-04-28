package org.cbioportal.model;

import java.io.Serializable;

public class Sample implements Serializable {
    private Integer internalId;

    private String stableId;

    private String sampleType;

    private Patient patient;

    private TypeOfCancer typeOfCancer;

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

    public String getSampleType() {
        return sampleType;
    }

    public void setSampleType(String sampleType) {
        this.sampleType = sampleType;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public TypeOfCancer getTypeOfCancer() {
        return typeOfCancer;
    }

    public void setTypeOfCancer(TypeOfCancer typeOfCancer) {
        this.typeOfCancer = typeOfCancer;
    }
}