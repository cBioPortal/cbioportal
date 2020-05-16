package org.mskcc.cbio.portal.util;

public class SurvivalAttributeUtil {

    private static final String ZERO_PREFIX = "0:";
    private static final String ONE_PREFIX = "1:";

    public enum SurvivalStatusAttributes {
        OS_STATUS("OS_STATUS"),
        DFS_STATUS("DFS_STATUS");

        String attributeId;
        SurvivalStatusAttributes(String attrId) {
            attributeId = attrId;
        }

        static public boolean has(String value) {
            try { 
                return valueOf(value) != null; 
            }
            catch (IllegalArgumentException x) { 
                return false;
            }
        }

        String getAttributeId() {
            return attributeId;
        }
    }

    public enum SurvivalStatusMappingOneValues {
        DECEASED("DECEASED"),
        RECURRED_PROGRESSED("Recurred/Progressed"),
        RECURRED("Recurred"),
        PROGRESSED("Progressed");

        String statusValue;
        SurvivalStatusMappingOneValues(String value) {
            statusValue = value;
        }

        static public boolean has(String value) {
            try { 
                value = value.replaceAll("[/]", "_");
                return valueOf(value.toUpperCase()) != null;
            }
            catch (IllegalArgumentException x) { 
                return false;
            }
        }

        String getStatusValue() {
            return statusValue;
        }
    }

    public enum SurvivalStatusMappingZeroValues {
        LIVING("LIVING"),
        DISEASEFREE("DiseaseFree");

        String statusValue;
        SurvivalStatusMappingZeroValues(String value) {
            statusValue = value;
        }

        static public boolean has(String value) {
            try { 
                return valueOf(value.toUpperCase()) != null;
            }
            catch (IllegalArgumentException x) { 
                return false;
            }
        }

        String getStatusValue() {
            return statusValue;
        }
    }

    public static String getModifiedAttributeValue(String value) {
        if (SurvivalStatusMappingOneValues.has(value)) {
            return ONE_PREFIX + value;
        } else if (SurvivalStatusMappingZeroValues.has(value)) {
            return ZERO_PREFIX + value;
        } else {
            // value is new format or is one of the NULL_VALUES
            return value;
        }
    }
}
