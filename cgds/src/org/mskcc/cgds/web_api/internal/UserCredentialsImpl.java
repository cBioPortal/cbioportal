// package
package org.mskcc.cgds.web_api.internal;

// imports
import org.mskcc.cgds.model.User;
import org.mskcc.cgds.model.UserAuthorities;
import org.mskcc.cgds.dao.DaoUser;
import org.mskcc.cgds.dao.DaoUserAuthorities;
import org.mskcc.cgds.dao.DaoException;
import org.mskcc.cgds.web_api.UserCredentials;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.security.access.prepost.PreAuthorize;

/**
 * The class which implements UserCredentials interface.
 * We take advantage of spring-security method security.
 *
 * @author Benjamin Gross
 */
public class UserCredentialsImpl implements UserCredentials {

	private static Log log = LogFactory.getLog(UserCredentialsImpl.class);

    /**
     * Get user auth tokens for given email address.
     *
     * @param email
     * @return Table output.
     * @throws DaoException Database Error.
	 *
	 * Additional level of method security.  Only users with ROLE_PORTAL|_ADMIN
	 * and a principal object of type ConsumerDetails (meaning we got here through OAUTH)
	 * are allowed to access this method.
	*/
	@PreAuthorize("hasRole('ROLE_PORTAL_ADMIN') and " +
				  "principal instanceof T(org.springframework.security.oauth.provider.ConsumerDetails)")
    public String getUserCredentials(String email) throws DaoException {

		if (log.isDebugEnabled()) {
			log.debug("email: " + email);
		}

		User user = DaoUser.getUserByEmail(email);
        StringBuffer buf = new StringBuffer();
        if (user != null && user.isEnabled()) {
			UserAuthorities userAuthorities = DaoUserAuthorities.getUserAuthorities(user);
			buf.append(user.getConsumerKey() + "\t" + user.getConsumerSecret() + "\t");
			for (String authority : userAuthorities.getAuthorities()) {
				buf.append(authority + ",");
			}
			buf.deleteCharAt(buf.length()-1);
			buf.append("\n");
		}
		else {
			buf.append("Error:  Unknown user or account disabled:  " + email + ".\n");
        }

		if (log.isDebugEnabled()) {
			log.debug("buffer: " + buf.toString());
		}

		// outta here
        return buf.toString();
    }
}

