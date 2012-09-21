/** Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
**
** This library is free software; you can redistribute it and/or modify it
** under the terms of the GNU Lesser General Public License as published
** by the Free Software Foundation; either version 2.1 of the License, or
** any later version.
**
** This library is distributed in the hope that it will be useful, but
** WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
** MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
** documentation provided hereunder is on an "as is" basis, and
** Memorial Sloan-Kettering Cancer Center 
** has no obligations to provide maintenance, support,
** updates, enhancements or modifications.  In no event shall
** Memorial Sloan-Kettering Cancer Center
** be liable to any party for direct, indirect, special,
** incidental or consequential damages, including lost profits, arising
** out of the use of this software and its documentation, even if
** Memorial Sloan-Kettering Cancer Center 
** has been advised of the possibility of such damage.  See
** the GNU Lesser General Public License for more details.
**
** You should have received a copy of the GNU Lesser General Public License
** along with this library; if not, write to the Free Software Foundation,
** Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
**/

// package
package org.mskcc.cbio.portal.openIDlogin;

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
}
