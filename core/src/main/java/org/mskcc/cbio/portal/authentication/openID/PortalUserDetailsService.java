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
package org.mskcc.cbio.portal.authentication.openID;

// imports
import org.mskcc.cbio.portal.model.User;
import org.mskcc.cbio.portal.model.UserAuthorities;
import org.mskcc.cbio.portal.dao.PortalUserDAO;
import org.mskcc.cbio.portal.authentication.PortalUserDetails;

import org.springframework.security.openid.*;

import org.springframework.security.core.userdetails.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;

import org.apache.commons.logging.*;

import java.util.List;

/**
 * Custom UserDetailsService which authenticates
 * OpenID user against backend cgds database.
 *
 * @author Benjamin Gross
 */
public class PortalUserDetailsService
	implements UserDetailsService, AuthenticationUserDetailsService<OpenIDAuthenticationToken> {

	// logger
	private static final Log log = LogFactory.getLog(PortalUserDetailsService.class);

	// ref to our user dao
    private final PortalUserDAO portalUserDAO;

    /**
     * Constructor.
     *
     * Takes a ref to PortalUserDAO used to authenticate registered
     * users in the database.
     *
     * @param portalUserDAO PortalUserDAO
     */
    public PortalUserDetailsService(PortalUserDAO portalUserDAO) {
        this.portalUserDAO = portalUserDAO;
    }
          

    /**
     * Implementation of {@code UserDetailsService}.
	 * We only need this to satisfy the {@code RememberMeServices} requirements.
     */
    public UserDetails loadUserByUsername(String id) throws UsernameNotFoundException {
		throw new UnsupportedOperationException();
    }


    /**
     * Implementation of {@code AuthenticationUserDetailsService}
	 * which allows full access to the submitted {@code Authentication} object.
	 * Used by the OpenIDAuthenticationProvider.
     */
    public UserDetails loadUserDetails(OpenIDAuthenticationToken token) throws UsernameNotFoundException {

		// what we return
		PortalUserDetails toReturn = null;

		// get open id
        String id = token.getIdentityUrl();
		id = id.toLowerCase();

		// grab other open id attributes
        String email = null;
        String firstName = null;
        String lastName = null;
        String fullName = null;

		// myopenid does not return attributes in the token
		if (id.indexOf("myopenid") != -1) {
			email = id;
			fullName = id;
		}
		else {
			List<OpenIDAttribute> attributes = token.getAttributes();
			for (OpenIDAttribute attribute : attributes) {
				if (attribute.getName().equals("email")) {
					email = attribute.getValues().get(0);
					email = email.toLowerCase();
				}
				if (attribute.getName().equals("firstname")) {
					firstName = attribute.getValues().get(0);
				}
				if (attribute.getName().equals("lastname")) {
					lastName = attribute.getValues().get(0);
				}
				if (attribute.getName().equals("fullname")) {
					fullName = attribute.getValues().get(0);
				}
			}
			if (fullName == null) {
				StringBuilder fullNameBldr = new StringBuilder();
				if (firstName != null) {
					fullNameBldr.append(firstName);
				}
				if (lastName != null) {
					fullNameBldr.append(" ").append(lastName);
				}
				fullName = fullNameBldr.toString();
			}
		}

		// check if this user exists in our backend db
		try {
            if (log.isDebugEnabled()) {
                log.debug("loadUserDetails(), attempting to fetch portal user, email: " + email);
            }
            User user = portalUserDAO.getPortalUser(email);
            if (user != null && user.isEnabled()) {
                if (log.isDebugEnabled()) {
                    log.debug("loadUserDetails(), attempting to fetch portal user authorities, email: " + email);
                }
                UserAuthorities authorities = portalUserDAO.getPortalUserAuthorities(email);
                if (authorities != null) {
                    List<GrantedAuthority> grantedAuthorities =
                        AuthorityUtils.createAuthorityList(authorities.getAuthorities().toArray(new String[authorities.getAuthorities().size()]));
                    toReturn = new PortalUserDetails(id, grantedAuthorities);
                    toReturn.setEmail(email);
                    toReturn.setName(fullName);
                }
            }
		}
		catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.debug(e.getMessage());
            }
            else {
                e.printStackTrace();
            }
		}

		// outta here
		if (toReturn == null) {
            if (log.isDebugEnabled()) {
                log.debug("loadUserDetails(), user and/or user authorities is null, email: " + email);
            }
			throw new UsernameNotFoundException("Error:  Unknown user or account disabled");
		}
		else {
            if (log.isDebugEnabled()) {
                log.debug("loadUserDetails(), successfully authenticated user, email: " + email);
            }
			return toReturn;
		}
    }
}
