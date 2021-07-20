package org.cbioportal.service.util;

public enum VariantType {
    FUSION("Fusion"),
    TRUNCATION("Deletion"),//TRUNCATION is of type deletion in oncokb
    TRANSLOCATION("Translocation"),
    INVERSION("Inversion"),
    DELETION("Deletion"),
    DUPLICATION("Duplication"),
    INTRAGENIC("Deletion");//INTRAGENIC is of type deletion in oncokb

    private String variantType;

    public String toString() {
        return variantType;
    }

    VariantType(String variantType) {
        this.variantType = variantType;
    }

    public String getVariantType() {
        return variantType;
    }
}
