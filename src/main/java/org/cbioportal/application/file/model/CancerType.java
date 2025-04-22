package org.cbioportal.application.file.model;

import java.util.LinkedHashMap;
import java.util.SequencedMap;

public class CancerType implements TableRow {
    private String typeOfCancerId;
    private String name;
    private String dedicatedColor;
    private String shortName;
    private String parent;

    public String getParent() {
        return parent;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getDedicatedColor() {
        return dedicatedColor;
    }

    public void setDedicatedColor(String dedicatedColor) {
        this.dedicatedColor = dedicatedColor;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTypeOfCancerId() {
        return typeOfCancerId;
    }

    public void setTypeOfCancerId(String typeOfCancerId) {
        this.typeOfCancerId = typeOfCancerId;
    }

    @Override
    public SequencedMap<String, String> toRow() {
        return new LinkedHashMap<>() {
            {
                put("TYPE_OF_CANCER_ID", typeOfCancerId);
                put("NAME", name);
                put("DEDICATED_COLOR", dedicatedColor);
                put("PARENT", parent);
            }
        };
    }
}
