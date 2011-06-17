package org.mskcc.portal.util;

/**
 * Utility class for getting / setting global properties.
 */
public class GlobalProperties {
    //  the default CGDS URL.
    private static String cgdsUrl = "http://cbio.mskcc.org/cgds-public/webservice.do";

    /**
     * Gets the Global CGDS URL.
     *
     * @return CGDS URL.
     */
    public static String getCgdsUrl() {
        return cgdsUrl;
    }

    /**
     * Sets the Global CGDS URL.
     *
     * @param url CGDS URL.
     */
    public static void setCgdsUrl(String url) {
        cgdsUrl = url;
    }
}
