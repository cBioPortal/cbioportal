package org.mskcc.cbio.portal.util;

import javax.servlet.http.HttpServletRequest;

/**
 * Provides Basic Information about a User, as Stored in the Session.
 * Also provides methods for logging/logging out, as defined within the session.
 *
 * @author Ethan Cerami.
 */
public class UserInfo {
    private static final String EMAIL = "email_address"; // key for email param stored in session

    /**
     * Is the User Currently Authenticated?  Determined via Session.
     *
     * @param request HttpServletRequest.
     * @return boolean
     */
    public static boolean isUserAuthenticated(HttpServletRequest request) {
        String email = getEmailId(request);
        if (email != null) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Gets the User's Email ID.  Determined via Session.
     *
     * @param request HttpServletRequest.
     * @return email address.
     */
    public static String getEmailId (HttpServletRequest request) {
        return (String) request.getSession().getAttribute(EMAIL);
    }

    /**
     * Registers the Users Session as Authenticated.
     *
     * @param emailAddress Email Address.
     * @param request HttpServletRequest.
     */
    public static void loginUser(String emailAddress,
            HttpServletRequest request) {
        request.getSession().setAttribute(EMAIL, emailAddress);
    }

    /**
     * Logs out the User.
     *
     * @param request HttpServletRequest.
     */
    public static void logoutUser(HttpServletRequest request) {
        request.getSession().removeAttribute(EMAIL);
    }
}
