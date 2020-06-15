package org.cbioportal.model;

public class GenericAssayAdditionalProperty {
    private String name;
    private String value;
    private String stableId;

    public GenericAssayAdditionalProperty(String name, String value, String stableId) {
        this.name = name;
        this.value = value;
        this.stableId = stableId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getStableId() {
        return stableId;
    }

    public void setStableId(String stableId) {
        this.stableId = stableId;
    }
}