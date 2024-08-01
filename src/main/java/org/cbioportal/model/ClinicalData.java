package org.cbioportal.model;

import jakarta.validation.constraints.NotNull;

public class ClinicalData extends UniqueKeyBase implements Binnable  {

    private Integer internalId;
    private String sampleId;
    @NotNull
    private String patientId;
    @NotNull
    private String studyId;
    @NotNull
    private String attrId;
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

    public Boolean isPatientAttribute() {
        if (clinicalAttribute == null) {
            return null;
        }
        return this.clinicalAttribute.getPatientAttribute();
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
