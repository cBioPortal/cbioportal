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
package org.mskcc.cbio.portal.model;

// imports
import java.util.Collection;

/**
 * User authorites bean.
 *
 * @author Benjamin Gross
 */
public class UserAuthorities {

	private String email;
	private Collection<String> authorities;
   
	/**
	 * Constructor.
	 */
	public UserAuthorities(String email, Collection<String> authorities) {
		this.email = email;
		this.authorities = authorities;
	}

	// accessors
	public String getEmail() { return email; }
	public void setEmail(String email) { this.email = email.toLowerCase(); }
	public Collection<String> getAuthorities() { return authorities; }
	public void setAuthorities(Collection<String> authorities) { this.authorities = authorities; }
}
