// package
package org.mskcc.cgds.model;

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
	public void setEmail(String email) { this.email = email; }
	public Collection<String> getAuthorities() { return authorities; }
	public void setAuthorities(Collection<String> authorities) { this.authorities = authorities; }
}
