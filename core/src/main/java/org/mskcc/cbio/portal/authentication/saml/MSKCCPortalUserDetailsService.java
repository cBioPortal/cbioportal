/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This file is part of cBioPortal.
 *
 * cBioPortal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
public class MSKCCPortalUserDetailsService implements SAMLUserDetailsService
{
    private static final String MSKCC_EMAIL_SUFFIX = "mskcc.org";
	private static final Log log = LogFactory.getLog(MSKCCPortalUserDetailsService.class);
    private static final Collection<String> defaultAuthorities = initializeDefaultAuthorities();
    private static final Collection<String> initializeDefaultAuthorities()
    {
        String appName = GlobalProperties.getAppName();
        Collection<String> toReturn = new ArrayList<String>();
        toReturn.add(appName + ":PUBLIC");
        toReturn.add(appName + ":EXTENDED");
        toReturn.add(appName + ":MSKPUB");
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
    public MSKCCPortalUserDetailsService(PortalUserDAO portalUserDAO) {
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

		// get userid and name
        String userid = credential.getAttributeAsString("/UserAttribute[@ldap:targetAttribute=\"mail\"]");
        String name = credential.getAttributeAsString("/UserAttribute[@ldap:targetAttribute=\"displayName\"]");

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
                    // ensure that granted authorities contains default (google spreadsheet may not have default authorities for all users)
                    if (userid.endsWith(MSKCC_EMAIL_SUFFIX)) {
                        grantedAuthorities.addAll(getDefaultGrantedAuthorities(userid));
                    }
                    toReturn = new PortalUserDetails(userid, grantedAuthorities);
                    toReturn.setEmail(userid);
                    toReturn.setName(userid);
                }
            }
		}
		catch (Exception e) {
            if (userid.endsWith(MSKCC_EMAIL_SUFFIX) && !GlobalProperties.getAppName().toLowerCase().contains("triage")) {
                if (log.isDebugEnabled()) {
                    log.debug("loadUserDetails(), granting default authorities for userid: " + userid);
                }
                toReturn = new PortalUserDetails(userid, getDefaultGrantedAuthorities(userid));
                //TBD - we need to get user name from SAML credential
                portalUserDAO.addPortalUser(new User(userid, name, true));
                portalUserDAO.addPortalUserAuthorities(new UserAuthorities(userid, defaultAuthorities));
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
