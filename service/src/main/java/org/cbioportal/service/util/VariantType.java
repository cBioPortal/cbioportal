package org.cbioportal.service.util;

public enum VariantType {
    FUSION("Fusion"),
    TRUNCATION("Truncation"),
    TRANSLOCATION("Translocation"),
    INVERSION("Inversion"),
    DELETION("Deletion"),
    DUPLICATION("Duplication"),
    INTRAGENIC("Intragenic");

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
