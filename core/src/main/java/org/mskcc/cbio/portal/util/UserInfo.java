/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This file is part of cBioPortal.
 *
 * cBioPortal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

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
