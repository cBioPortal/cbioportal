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

package org.cbioportal.security.spring.authentication.saml;

// imports
import org.cbioportal.model.User;
import org.cbioportal.model.UserAuthorities;
import org.cbioportal.persistence.SecurityRepository;
import org.cbioportal.security.spring.authentication.PortalUserDetails;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.security.saml.*;
import org.springframework.security.saml.userdetails.*;

import org.springframework.security.core.userdetails.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;

import org.springframework.stereotype.Service;

import org.apache.commons.logging.*;

import java.util.*;

/**
 * Custom UserDetailsService which authenticates
 * SAML user against backend cgds database.
 *
 * @author Benjamin Gross
 */
@Service
public class MSKCCPortalUserDetailsService implements SAMLUserDetailsService
{

  private static String appName;
  @Value("${app.name}:public_portal")
  public void setAppName(String property) { this.appName = property; }


	private static final List<String> mskEmailSuffixes = initializeDefaultEmailSuffixes();
	private static final List<String> initializeDefaultEmailSuffixes()
	{
		List<String> toReturn = new ArrayList<String>();
		toReturn.add("mskcc.org");
		toReturn.add("sloankettering.edu");
		return toReturn;
	}
	private static final Log log = LogFactory.getLog(MSKCCPortalUserDetailsService.class);
    private static final List<String> defaultAuthorities = initializeDefaultAuthorities();
    private static final List<String> initializeDefaultAuthorities()
    {
        List<String> toReturn = new ArrayList<String>();
        toReturn.add(appName + ":PUBLIC");
        toReturn.add(appName + ":EXTENDED");
        toReturn.add(appName + ":MSKPUB");
        return toReturn;
    }

    @Autowired
    private SecurityRepository securityRepository;

    /**
     * Constructor.
     *
     */
    public MSKCCPortalUserDetailsService() {
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
            User user = securityRepository.getPortalUser(userid);
            if (user != null && user.isEnabled()) {
                if (log.isDebugEnabled()) {
                    log.debug("loadUserDetails(), attempting to fetch portal user authorities, userid: " + userid);
                }
                UserAuthorities authorities = securityRepository.getPortalUserAuthorities(userid);
                if (authorities != null) {
                    List<GrantedAuthority> grantedAuthorities =
                        AuthorityUtils.createAuthorityList(authorities.getAuthorities().toArray(new String[authorities.getAuthorities().size()]));
                    // ensure that granted authorities contains default (google spreadsheet may not have default authorities for all users)
                    if (mskUser(userid)) {
                        grantedAuthorities.addAll(getDefaultGrantedAuthorities(userid));
                    }
                    toReturn = new PortalUserDetails(userid, grantedAuthorities);
                    toReturn.setEmail(userid);
                    toReturn.setName(userid);
                }
            }
		}
		catch (Exception e) {
            if (mskUser(userid) && !appName.toLowerCase().contains("triage")) {
                if (log.isDebugEnabled()) {
                    log.debug("loadUserDetails(), granting default authorities for userid: " + userid);
                }
                toReturn = new PortalUserDetails(userid, getDefaultGrantedAuthorities(userid));
                //TBD - we need to get user name from SAML credential
                securityRepository.addPortalUser(new User(userid, name, true));
                securityRepository.addPortalUserAuthorities(new UserAuthorities(userid, defaultAuthorities));
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

	private boolean mskUser(final String email)
	{
		for (String suffix : mskEmailSuffixes) {
			if (email.endsWith(suffix)) {
				return true;
			}
		}
		return false;
	}

    private List<GrantedAuthority> getDefaultGrantedAuthorities(final String username)
    {
        List<String> defAuthorities = new ArrayList<String>(defaultAuthorities);
        defAuthorities.add(appName + ":" + username.substring(0, username.indexOf("@")).toUpperCase());
        UserAuthorities authorities = new UserAuthorities(username, defAuthorities);
        return AuthorityUtils.createAuthorityList(authorities.getAuthorities().toArray(new String[authorities.getAuthorities().size()]));

    }
}
