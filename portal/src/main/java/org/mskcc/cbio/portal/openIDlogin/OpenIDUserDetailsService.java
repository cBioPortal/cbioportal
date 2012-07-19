// package
package org.mskcc.cbio.portal.openIDlogin;

// imports
import org.mskcc.cgds.model.User;
import org.mskcc.cgds.model.UserAuthorities;
import org.mskcc.portal.dao.PortalUserDAO;

import org.springframework.security.openid.OpenIDAttribute;
import org.springframework.security.openid.OpenIDAuthenticationToken;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.core.authority.AuthorityUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;

/**
 * Custom UserDetailsService which authenticates
 * OpenID user against backend cgds database.
 *
 * @author Benjamin Gross
 */
public class OpenIDUserDetailsService
	implements UserDetailsService, AuthenticationUserDetailsService<OpenIDAuthenticationToken> {

	// logger
	private static Log log = LogFactory.getLog(OpenIDUserDetailsService.class);

	// ref to our user dao
    private PortalUserDAO portalUserDAO;

    /**
     * Constructor.
     *
     * Takes a ref to PortalUserDAO used to authenticate registered
     * users in the database.
     *
     * @param portalUserDAO PortalUserDAO
     */
    public OpenIDUserDetailsService(PortalUserDAO portalUserDAO) {
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
		OpenIDUserDetails toReturn = null;

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
                        AuthorityUtils.createAuthorityList(authorities.getAuthorities().toArray(new String[0]));
                    toReturn = new OpenIDUserDetails(id, grantedAuthorities);
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
