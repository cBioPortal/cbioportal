package org.cbioportal.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;

public class ClinicalData extends UniqueKeyBase {

    @JsonProperty("internalId")
    private Integer internalId;
    @JsonProperty("sampleId")
    private String sampleId;
    @NotNull
    @JsonProperty("patientId")
    private String patientId;
    @NotNull
    @JsonProperty("studyId")
    private String studyId;
    @NotNull
    @JsonProperty("attrId")
    private String attrId;
    @JsonProperty("attrValue")
    private String attrValue;
    private ClinicalAttribute clinicalAttribute;

    public Integer getInternalId() {
        return internalId;
    }

    public void setInternalId(Integer internalId) {
        this.internalId = internalId;
    }

    public String getSampleId() {
        return sampleId;
    }

    public void setSampleId(String sampleId) {
        this.sampleId = sampleId;
    }

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public String getStudyId() {
        return studyId;
    }

    public void setStudyId(String studyId) {
        this.studyId = studyId;
    }

    public String getAttrId() {
        return attrId;
    }

    public void setAttrId(String attrId) {
        this.attrId = attrId;
    }

    public String getAttrValue() {
        return attrValue;
    }

    public void setAttrValue(String attrValue) {
        this.attrValue = attrValue;
    }

    public ClinicalAttribute getClinicalAttribute() {
        return clinicalAttribute;
    }

    public void setClinicalAttribute(ClinicalAttribute clinicalAttribute) {
        this.clinicalAttribute = clinicalAttribute;
    }
}
