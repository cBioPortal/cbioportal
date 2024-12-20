package org.cbioportal.persistence.enums;

public enum ClinicalAttributeDataType {
    CATEGORICAL("CATEGORICAL"),
    NUMERIC("NUMERIC");
    
    private final String value;
    
    ClinicalAttributeDataType(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return this.value;
    }
}
