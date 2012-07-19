// package
package org.mskcc.cbio.portal.dao;

// imports
import org.mskcc.cbio.cgds.model.User;
import org.mskcc.cbio.cgds.model.UserAuthorities;

/**
 * Interface to use to retrieve
 * portal user information.
 */
public interface PortalUserDAO {

	/**
	 * Given a user id, returns a user instance.
	 * If username does not exist in db, returns null.
     *
     * @param username String
     * @return User
	 */
	User getPortalUser(String username);

	/**
	 * Given a user id, returns a UserAuthorities instance.
	 * If username does not exist in db, returns null.
     *
     * @param username String
     * @return User
	 */
	UserAuthorities getPortalUserAuthorities(String username);
}