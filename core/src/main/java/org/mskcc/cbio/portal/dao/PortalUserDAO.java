/** Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 * documentation provided hereunder is on an "as is" basis, and
 * Memorial Sloan-Kettering Cancer Center 
 * has no obligations to provide maintenance, support,
 * updates, enhancements or modifications.  In no event shall
 * Memorial Sloan-Kettering Cancer Center
 * be liable to any party for direct, indirect, special,
 * incidental or consequential damages, including lost profits, arising
 * out of the use of this software and its documentation, even if
 * Memorial Sloan-Kettering Cancer Center 
 * has been advised of the possibility of such damage.
*/

// package
package org.mskcc.cbio.portal.dao;

// imports
import org.mskcc.cbio.portal.model.User;
import org.mskcc.cbio.portal.model.UserAuthorities;

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