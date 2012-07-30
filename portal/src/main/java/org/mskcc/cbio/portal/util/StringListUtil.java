package org.mskcc.cbio.portal.util;

import java.util.ArrayList;

/**
 * Misc. Utilities for Working with Lists of Items and Converting them to Strings.
 */
public class StringListUtil {
    private static final String COMMA = ", ";
    private static final String PERIOD = ".";

    /**
     * Converts a List of Items into a sentence with commas and periods.
     * For example, the following list of items: apple banana orange
     * is converted into: "apple, banana, organge."
     *
     * @param items List of Items.
     * @return Sentence of Items.
     */
    public static String covertItemsIntoSentence (ArrayList<String> items) {
        StringBuffer str = new StringBuffer();
        for (int i = 0; i < items.size(); i++) {
            String item = items.get(i);
            str.append(item);
            str.append(getDelimeter(i, items));
        }
        return str.toString();
    }

    private static String getDelimeter(int i, ArrayList<String> items) {
        if (i < items.size() - 1) {
            return COMMA;
        } else {
            return PERIOD;
        }
    }

}
