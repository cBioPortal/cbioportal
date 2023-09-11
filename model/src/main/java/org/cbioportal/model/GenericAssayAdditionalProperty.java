package org.cbioportal.model;

import java.io.Serializable;
import java.util.Objects;

public class GenericAssayAdditionalProperty implements Serializable {
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

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof GenericAssayAdditionalProperty)) {
            return false;
        }
        GenericAssayAdditionalProperty that = (GenericAssayAdditionalProperty) o;
        return getName().equals(that.getName()) &&
            getValue().equals(that.getName()) &&
            getStableId().equals(that.getStableId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getValue(), getStableId());
    }
}
