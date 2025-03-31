package org.cbioportal.application.file.model;

public class ClinicalPatientAttributeValue {
    private String patientId;
    private String attributeId;
    private String attributeValue;

    public ClinicalPatientAttributeValue() {
        super();
    }

    public ClinicalPatientAttributeValue(String patientId, String attributeId, String attributeValue) {
        this.patientId = patientId;
        this.attributeId = attributeId;
        this.attributeValue = attributeValue;
    }

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
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