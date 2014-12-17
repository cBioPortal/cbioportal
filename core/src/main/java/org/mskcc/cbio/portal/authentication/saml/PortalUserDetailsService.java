/** Copyright (c) 2014 Memorial Sloan-Kettering Cancer Center.
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
package org.mskcc.cbio.portal.authentication.saml;

// imports
import org.mskcc.cbio.portal.model.User;
import org.mskcc.cbio.portal.model.UserAuthorities;
import org.mskcc.cbio.portal.dao.PortalUserDAO;
import org.mskcc.cbio.portal.authentication.PortalUserDetails;
import org.mskcc.cbio.portal.util.GlobalProperties;

import org.springframework.security.saml.*;
import org.springframework.security.saml.userdetails.*;

import org.springframework.security.core.userdetails.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;

import org.apache.commons.logging.*;

import java.util.*;

/**
 * Custom UserDetailsService which authenticates
 * SAML user against backend cgds database.
 *
 * @author Benjamin Gross
 */
public class PortalUserDetailsService implements SAMLUserDetailsService
{
	private static final Log log = LogFactory.getLog(PortalUserDetailsService.class);
    private static final Collection<String> defaultAuthorities = initializeDefaultAuthorities();
    private static final Collection<String> initializeDefaultAuthorities()
    {
        String appName = GlobalProperties.getAppName();
        Collection<String> toReturn = new ArrayList<String>();
        toReturn.add(appName + ":PUBLIC");
        toReturn.add(appName + ":EXTENDED");
        toReturn.add(appName + ":DMP");
        return toReturn;
    }

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
     * Implementation of {@code SAMLUserDetailsService}.
     */
    @Override
    public Object loadUserBySAML(SAMLCredential credential)
    {
		// what we return
		PortalUserDetails toReturn = null;

		// get user id
        String userid = credential.getNameID().getValue().toLowerCase();

		// check if this user exists in our backend db
		try {
            if (log.isDebugEnabled()) {
                log.debug("loadUserDetails(), attempting to fetch portal user, userid: " + userid);
            }
            User user = portalUserDAO.getPortalUser(userid);
            if (user != null && user.isEnabled()) {
                if (log.isDebugEnabled()) {
                    log.debug("loadUserDetails(), attempting to fetch portal user authorities, userid: " + userid);
                }
                UserAuthorities authorities = portalUserDAO.getPortalUserAuthorities(userid);
                if (authorities != null) {
                    List<GrantedAuthority> grantedAuthorities =
                        AuthorityUtils.createAuthorityList(authorities.getAuthorities().toArray(new String[authorities.getAuthorities().size()]));
                    toReturn = new PortalUserDetails(userid, grantedAuthorities);
                    toReturn.setEmail(userid);
                    toReturn.setName(userid);
                }
            }
		}
		catch (Exception e) {
            if (userid.endsWith("mskcc.org")) {
                if (log.isDebugEnabled()) {
                    log.debug("loadUserDetails(), granting default authorities for userid: " + userid);
                }
                toReturn = new PortalUserDetails(userid, getDefaultGrantedAuthorities(userid));
                toReturn.setEmail(userid);
                toReturn.setName(userid);
            }
            else {
                if (log.isDebugEnabled()) {
                    log.debug(e.getMessage());
                }
                else {
                    e.printStackTrace();
                }
            }
		}

		// outta here
		if (toReturn == null) {
            if (log.isDebugEnabled()) {
                log.debug("loadUserDetails(), user and/or user authorities is null, userid: " + userid);
            }
			throw new UsernameNotFoundException("Error:  Unknown user or account disabled");
		}
		else {
            if (log.isDebugEnabled()) {
                log.debug("loadUserDetails(), successfully authenticated user, userid: " + userid);
            }
			return toReturn;
		}
    }

    private List<GrantedAuthority> getDefaultGrantedAuthorities(final String username)
    {
        UserAuthorities authorities = new UserAuthorities(username, defaultAuthorities);
        return AuthorityUtils.createAuthorityList(authorities.getAuthorities().toArray(new String[authorities.getAuthorities().size()]));

    }
}
