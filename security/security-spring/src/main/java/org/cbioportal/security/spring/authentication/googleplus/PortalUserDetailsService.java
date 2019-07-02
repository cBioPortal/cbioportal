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

package org.cbioportal.security.spring.authentication.googleplus;

import org.cbioportal.model.User;
import org.cbioportal.model.UserAuthorities;
import org.cbioportal.persistence.SecurityRepository;
import org.cbioportal.security.spring.authentication.PortalUserDetails;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.base.*;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.security.core.userdetails.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;

import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Responsible for verifying that a social site user name has been registered in the
 * portal database For registered users, an instance of GoogleplusUserDetails is
 * completed and returned. Null is returned for non-registered users
 *
 * Implementation based on code in OpenIDUserDetailsService
 *
 * @author criscuof
 */
@Service
public class PortalUserDetailsService implements UserDetailsService {

    private static final Log log = LogFactory.getLog(PortalUserDetailsService.class);

    @Autowired
    private SecurityRepository securityRepository;

    /**
     * Constructor.
     *
     */
    public PortalUserDetailsService() {
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(username), "A username is required");
        // set the username into the global state so other components can find out who
        // logged in or tried to log in most recently
        if (log.isDebugEnabled()) {
            log.debug("loadUserByUsername(), attempting to fetch portal user, email: " + username);
        }
        PortalUserDetails toReturn = null;
        User user = null;
        try {
            user = securityRepository.getPortalUser(username);
        } catch (Exception e ){
            log.debug("User " +username +" was not found in the cbio users table");
            log.debug("Error:" + e);
        }
        if (user != null && user.isEnabled()) {
            if (log.isDebugEnabled()) {
                log.debug("loadUserByUsername(), attempting to fetch portal user authorities, username: " + username);
            }
            UserAuthorities authorities = securityRepository.getPortalUserAuthorities(username);
            if (authorities != null) {
                List<GrantedAuthority> grantedAuthorities
                        = AuthorityUtils.createAuthorityList(
                                authorities.getAuthorities().toArray(new String[authorities.getAuthorities().size()]));
                toReturn = new PortalUserDetails(username, grantedAuthorities);
                toReturn.setEmail(user.getEmail());
                toReturn.setName(user.getName());
             
                
            }
        }

        // outta here
        if (toReturn == null) {
            if (log.isDebugEnabled()) {
                log.debug("loadUserByUsername(), user and/or user authorities is null, user name: " +username);
            }
            // use the Exception message to attache the username to the request object
            throw new UsernameNotFoundException(username);
        }    
        else {
            if (log.isDebugEnabled()) {
                log.debug("loadUserByUsername(), successfully authenticated user, user name: " + username);
            }
            return toReturn;
        }
    }
}
