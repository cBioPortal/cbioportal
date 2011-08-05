package org.mskcc.portal.util;

/**
 * Utility class for getting / setting global properties.
 */
public class GlobalProperties {
    //  the default CGDS URL.
    private static String cgdsUrl = "http://cbio.mskcc.org/cgds-public/webservice.do";
	// the default CGDS Credentials URL
	private static String cgdsCredentialsUrl = "http://cbio.mskcc.org/cgds-public/credentials.do";
    //  the default Pathway Commons URL.
    private static String pathwayCommonsUrl = "http://www.pathwaycommons.org/pc2";
	// the default authenticate
	private static boolean authenticate = true;

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

    /**
     * Gets the Global CGDS Credentials URL.
     *
     * @return CGDS Credentias URL.
     */
    public static String getCgdsCredentialsUrl() {
        return cgdsCredentialsUrl;
    }

    /**
     * Sets the Global CGDS Credentials URL.
     *
     * @param url CGDS Credentials URL.
     */
    public static void setCgdsCredentialsUrl(String credentialsUrl) {
        cgdsCredentialsUrl = credentialsUrl;
    }

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

	/**
	 * Gets the Global authenticate flag.
	 *
	 * @return authenticate
	 */
	public static boolean authenticate() {
		return authenticate;
	}

	/**
	 * Sets the Global authenticate flag.
	 * 
	 * @param authenticate boolean
	 */
	public static void setAuthenticate(boolean authenticate) {
		authenticate = authenticate;
	}
}
