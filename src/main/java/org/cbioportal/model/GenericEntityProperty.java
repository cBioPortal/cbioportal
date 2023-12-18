package org.cbioportal.model;

import java.io.Serializable;

public class GenericEntityProperty implements Serializable {
    private String name;
    private String value;
    private Integer entityId;

    public GenericEntityProperty(String name, String value, Integer entityId) {
        this.name = name;
        this.value = value;
        this.entityId = entityId;
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

    public Integer getEntityId() {
        return entityId;
    }

    public void setEntityId(Integer entityId) {
        this.entityId = entityId;
    }
}
