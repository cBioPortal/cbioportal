package org.cbioportal.persistence.enums;

public enum ClinicalAttributeDataSource {
    PATIENT("PATIENT"),SAMPLE("SAMPLE");
    
    private final String value;
    
    ClinicalAttributeDataSource(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return this.value;
    }
}
