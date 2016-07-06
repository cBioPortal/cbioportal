package org.cbioportal.model;

import java.util.Arrays;
import java.util.List;

public enum SampleType {

    PRIMARY_SOLID_TUMOR("Primary Solid Tumor"),
    RECURRENT_SOLID_TUMOR("Recurrent Solid Tumor"),
    PRIMARY_BLOOD_TUMOR("Primary Blood Tumor"),
    RECURRENT_BLOOD_TUMOR("Recurrent Blood Tumor"),
    METASTATIC("Metastatic"),
    BLOOD_NORMAL("Blood Derived Normal"),
    SOLID_NORMAL("Solid Tissues Normal");

    private String name;
    private static List<String> normalTypes = Arrays.asList(BLOOD_NORMAL.name, SOLID_NORMAL.name);
    private static List<String> nonNormalTypes = Arrays.asList(PRIMARY_SOLID_TUMOR.name, RECURRENT_SOLID_TUMOR.name,
            PRIMARY_BLOOD_TUMOR.name, RECURRENT_BLOOD_TUMOR.name, METASTATIC.name);

    SampleType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static List<String> getNormalTypes() {
        return normalTypes;
    }

    public static List<String> getNonNormalTypes() {
        return nonNormalTypes;
    }
}
