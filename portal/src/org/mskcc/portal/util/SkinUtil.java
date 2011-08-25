package org.mskcc.portal.util;

/**
 * Utility Class Containing Skin Configuration Options.
 *
 * @author Ethan Cerami.
 */
public class SkinUtil {

    /**
     * Gets the Site Title.
     * @return site title.
     */
    public static String getTitle() {
        Config config = Config.getInstance();
        return config.getProperty("skin.title");
    }

    /**
     * Gets the Site Blurb.
     * @return site blurb.
     */
    public static String getBlurb() {
        Config config = Config.getInstance();
        return config.getProperty("skin.blurb");
    }

    /**
     * Gets the Site Email Contact.
     * @return site email contact.
     */
    public static String getEmailContact() {
        Config config = Config.getInstance();
        return config.getProperty("skin.email_contact");
    }

    /**
     * Determines if users must authenticate or not.
     * @return true or false.
     */
    public static boolean usersMustAuthenticate() {
        Config config = Config.getInstance();
		return new Boolean(config.getProperty("authenticate"));
    }
}