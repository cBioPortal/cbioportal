package org.mskcc.cbio.maf;

public class ValueTypeUtil {

    private ValueTypeUtil() {
        throw new IllegalStateException("This is a utility class. Do not instantiate.");
    } 

    public static boolean isInt(String value) {
        if (value == null) {
            return false;
        }
        try {
            Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

    public static boolean isFloat(String value) {
        if (value == null) {
            return false;
        }
        try {
            Float.parseFloat(value);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

    public static boolean isDouble(String value) {
        if (value == null) {
            return false;
        }
        try {
            Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

}
