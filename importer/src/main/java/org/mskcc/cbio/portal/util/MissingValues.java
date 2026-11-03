package org.mskcc.cbio.portal.util;

public enum MissingValues {
    NOT_APPLICABLE("Not Applicable"),
    NOT_AVAILABLE("Not Available"),
    PENDING("Pending"),
    DISCREPANCY("Discrepancy"),
    COMPLETED("Completed"),
    NULL("null"), MISSING(""),
    NA("NA");

    private String propertyName;

    MissingValues(String propertyName) {
        this.propertyName = propertyName;
    }

    public String toString() {
        return propertyName;
    }

    static public boolean has(String value) {
        if (value == null)
            return false;
        if (value.trim().equals(""))
            return true;
        try {
            value = value.replaceAll("[\\[|\\]]", "");
            value = value.replaceAll(" ", "_");
            return valueOf(value.toUpperCase()) != null;
        } catch (IllegalArgumentException x) {
            return false;
        }
    }

    static public String getNotAvailable() {
        return "[" + NOT_AVAILABLE.toString() + "]";
    }
}
