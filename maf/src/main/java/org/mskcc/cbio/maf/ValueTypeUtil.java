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

    public static Float toFloat(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Integer) {
            return ((Integer) value).floatValue();
        }
        if (value instanceof Float) {
            return (Float) value;
        }
        if (value instanceof Long) {
            return ((Long) value).floatValue();
        }
        if (value instanceof Double) {
            return ((Double) value).floatValue();
        }
        if (value instanceof String) {
            return Float.parseFloat((String) value);
        }
        throw new RuntimeException("Object type not covered by toFloat method. Value is: " + value.toString());
    }

    public static Integer toInt(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Integer) {
            return (Integer) value;
        }
        if (value instanceof Float) {
            return Math.round((Float) value);
        }
        if (value instanceof Long) {
            return ((Long) value).intValue();
        }
        if (value instanceof Double) {
            return Math.toIntExact(Math.round((Double) value));
        }
        if (value instanceof String) {
            return Integer.parseInt((String) value);
        }
        throw new RuntimeException("Object type not covered by toInt method. Value is: " + value.toString());
    }

}
