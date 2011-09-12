package org.mskcc.portal.util;

/**
 * Utility Class Containing Skin Configuration Options.
 *
 * @author Ethan Cerami.
 */
public class SkinUtil {
    public static final String DEFAULT_TITLE = "cBio Cancer Genomics Portal";

    /**
     * Gets the Site Title.
     * @return site title.
     */
    public static String getTitle() {
        Config config = Config.getInstance();
        String skinTitle = config.getProperty("skin.title");
        if (skinTitle == null) {
            return DEFAULT_TITLE;
        } else {
            return skinTitle;
        }
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
     * Gets the Site Tag Line.
     * @return site tag line.
     */
    public static String getTagLineImage() {
        Config config = Config.getInstance();
        String tagLineImage = config.getProperty("skin.tag_line_image");
        if (tagLineImage == null) {
            tagLineImage = "images/tag_line.png";
        } else {
            tagLineImage = "images/" + tagLineImage;
        }
        return tagLineImage;
    }

    /**
     * Gets the Site Header Image.
     * @return site header image.
     */
    public static String getHeaderImage() {
        Config config = Config.getInstance();
        String headerImage = config.getProperty("skin.header_image");
        if (headerImage == null) {
            headerImage = "images/site_name.png";
        } else {
            headerImage = "images/" + headerImage;
        }
        return headerImage;
    }

    /**
     * Gets the Site Email Contact.
     * Emails should be in the form of:  xxx AT yyy DOT com.
     * @return site email contact.
     */
    public static String getEmailContact() {
        Config config = Config.getInstance();
        String emailAddress = config.getProperty("skin.email_contact");

        //  Return email address within mailme span, so that we can de-obfuscate with JQuery.
        return ("<span class=\"mailme\" title=\"Contact us\">" + emailAddress + "</span>");
    }

    /**
     * Determines if users must authenticate or not.
     * @return true or false.
     */
    public static boolean usersMustAuthenticate() {
        Config config = Config.getInstance();
		return new Boolean(config.getProperty("authenticate"));
    }
    
    /**
     * Determines whether to include networks
     * @return true or false
     */
    public static boolean includeNetworks() {
        Config config = Config.getInstance();
        return Boolean.parseBoolean(config.getProperty("include_networks"));
    }
}