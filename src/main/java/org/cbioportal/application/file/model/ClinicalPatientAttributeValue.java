package org.cbioportal.application.file.model;

import java.util.LinkedHashMap;
import java.util.SequencedMap;

public class ClinicalPatientAttributeValue implements ClinicalAttributeValue {
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

    public SequencedMap<String, String> getKey() {
        var key = new LinkedHashMap<String, String>();
        key.put(ClinicalAttribute.PATIENT_ID.getAttributeId(), patientId);
        return key;
    }

    public SequencedMap<String, String> getValue() {
        var value = new LinkedHashMap<String, String>();
        if (attributeId != null) {
            value.put(attributeId, attributeValue);
        }
        return value;
    }
}