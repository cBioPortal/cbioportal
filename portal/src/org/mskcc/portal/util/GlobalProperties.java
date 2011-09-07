package org.mskcc.portal.util;

/**
 * Utility class for getting / setting global properties.
 */
public class GlobalProperties {
    //  the default Pathway Commons URL.
    private static String pathwayCommonsUrl = "http://www.pathwaycommons.org/pc2";

    /**
     * Gets the Global Pathway Commons URL.
     *
     * @return Pathway Commons URL.
     */
    public static String getPathwayCommonsUrl() {
        return pathwayCommonsUrl;
    }

    /**
     * Sets the Global Pathway Commons URL.
     *
     * @param url Pathway Commons URL.
     */
    public static void setPathwayCommonsUrl(String url) {
        pathwayCommonsUrl = url;
    }
}
