package org.cbioportal.application.file.model;

public class ClinicalSampleAttributeValue {
    private String patientId;
    private String sampleId;
    private String attributeId;
    private String attributeValue;

    public ClinicalSampleAttributeValue() {
        super();
    }

    public ClinicalSampleAttributeValue(String patientId, String sampleId, String attributeId, String attributeValue) {
        this.patientId = patientId;
        this.sampleId = sampleId;
        this.attributeId = attributeId;
        this.attributeValue = attributeValue;
    }

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

    public String getAttributeId() {
        return attributeId;
    }

    public void setAttributeId(String attributeId) {
        this.attributeId = attributeId;
    }

    public String getAttributeValue() {
        return attributeValue;
    }

    public void setAttributeValue(String attributeValue) {
        this.attributeValue = attributeValue;
    }
}