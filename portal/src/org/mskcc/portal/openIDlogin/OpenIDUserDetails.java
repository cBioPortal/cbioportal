// package
package org.mskcc.portal.openIDlogin;

// imports
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

/**
 * A class which extends User and provides
 * methods to set and get properties obtained
 * via OpenID authentication.
 *
 * Properties have also been added to store
 * CGDS credentials rather than create
 * another "portal user" class.
 *
 * @author Benjamin Gross
 */
public class OpenIDUserDetails extends User {

    private String email;
    private String name;
	private String consumerKey;
	private String consumerSecret;

	/**
	 * Constructor.
	 *
	 * @param username String
	 * @param authorities Collection<GrantedAuthority>
	 *
	 * Username is what is presented to the authentication provider.
	 * Authorities is what should  be granted to the caller.
	 */
    public OpenIDUserDetails(String username, Collection<GrantedAuthority> authorities) {
        super(username, "unused", authorities);
    }

	// accessors
    public String getEmail() { return email; }
    public void setEmail(String email) {this.email = email; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
	public String getConsumerKey() { return consumerKey; }
	public void setConsumerKey(String consumerKey) { this.consumerKey = consumerKey; }
	public String getConsumerSecret() { return consumerSecret; }
	public void setConsumerSecret(String consumerSecret) { this.consumerSecret = consumerSecret; }
}
