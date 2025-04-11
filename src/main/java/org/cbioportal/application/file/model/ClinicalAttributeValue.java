package org.cbioportal.application.file.model;

public class ClinicalAttributeValue {
    private Long rowKey;
    private String attributeId;
    private String attributeValue;

    public ClinicalAttributeValue() {
        super();
    }

    public ClinicalAttributeValue(Long rowKey, String attributeId, String attributeValue) {
        this.rowKey = rowKey;
        this.attributeId = attributeId;
        this.attributeValue = attributeValue;
    }

    public Long getRowKey() {
        return rowKey;
    }

    public void setRowKey(Long rowKey) {
        this.rowKey = rowKey;
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