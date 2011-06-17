package org.mskcc.portal.util;

public class SkinUtil {

    public static String getTitle() {
        Config config = Config.getInstance();
        return config.getProperty("skin.title");
    }

    public static String getBlurb() {
        Config config = Config.getInstance();
        return config.getProperty("skin.blurb");
    }

    /**
     * Determines if users must authenticate or not.
     * @return true or false.
     */
    public static boolean usersMustAuthenticate() {
        Config config = Config.getInstance();
        String authentication =  config.getProperty("authenticate");
        if (authentication != null && authentication.startsWith("T")) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Gets the Global Authentication User Name.
     * @return Authentication User Name.
     */
    public static String getAuthenticationUserName() {
        Config config = Config.getInstance();
        String globalUserName = config.getProperty("authenticate.user_name");
        if (globalUserName == null) {
            globalUserName = "guest";
        }
        return globalUserName;
    }

    /**
     * Gets the Global Authentication Password.
     * @return Authentication Password.
     */
    public static String getAuthenticationPassword() {
        Config config = Config.getInstance();
        String globalPassword = config.getProperty("authenticate.password");
        if (globalPassword == null) {
            globalPassword = "guest";
        }
        return globalPassword;
    }

}
