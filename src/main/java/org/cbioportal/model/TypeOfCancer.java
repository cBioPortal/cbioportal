package org.cbioportal.model;

import java.io.Serializable;
import jakarta.validation.constraints.NotNull;

public class TypeOfCancer implements Serializable {

    @NotNull
    private String typeOfCancerId;
    private String name;
    private String dedicatedColor;
    private String shortName;
    private String parent;

    public String getTypeOfCancerId() {
        return typeOfCancerId;
    }

    public void setTypeOfCancerId(String typeOfCancerId) {
        this.typeOfCancerId = typeOfCancerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDedicatedColor() {
        return dedicatedColor;
    }

    public void setDedicatedColor(String dedicatedColor) {
        this.dedicatedColor = dedicatedColor;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getParent() {
        return parent;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }
}