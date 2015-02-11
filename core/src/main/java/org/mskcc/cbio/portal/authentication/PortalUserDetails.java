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
package org.mskcc.cbio.portal.authentication;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

/**
 * A class which extends User and provides
 * methods to set and get properties obtained
 * via an authentication protocol.
 *
 * @author Benjamin Gross
 */
public class PortalUserDetails extends User {

    private String email;
    private String name;

	/**
	 * Constructor.
	 *
	 * @param username String
	 * @param authorities Collection<GrantedAuthority>
	 *
	 * Username is what is presented to the authentication provider.
	 * Authorities is what should  be granted to the caller.
	 */
    public PortalUserDetails(String username, Collection<GrantedAuthority> authorities) {
        super(username, "unused", authorities);
    }

	// accessors
    public String getEmail() { return email; }
    public void setEmail(String email) {this.email = email; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
