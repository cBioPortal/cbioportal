package org.mskcc.cbio.portal.util;

public class DataValidator {
    public static boolean isValidNumericSequence(String str) {
        return str.matches("[0-9]+");
    }
}
