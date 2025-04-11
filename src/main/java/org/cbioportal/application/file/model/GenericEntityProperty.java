package org.cbioportal.application.file.model;

public class GenericEntityProperty {

    private Integer geneticEntityId;
    private String name;
    private String value;

    public Integer getGeneticEntityId() {
        return geneticEntityId;
    }

    public void setGeneticEntityId(Integer geneticEntityId) {
        this.geneticEntityId = geneticEntityId;
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
}
