package org.cbioportal.persistence.enums;

public enum DataSource {
    PATIENT("PATIENT"),SAMPLE("SAMPLE");
    
    private final String value;
    
    DataSource(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return this.value;
    }
}
