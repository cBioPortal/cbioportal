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
package org.mskcc.cbio.portal.authentication.activedirectory;

import org.mskcc.cbio.portal.model.User;
import org.mskcc.cbio.portal.model.UserAuthorities;
import org.mskcc.cbio.portal.dao.PortalUserDAO;
import org.mskcc.cbio.portal.authentication.PortalUserDetails;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.security.core.userdetails.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.ldap.core.*;
import org.springframework.security.ldap.userdetails.UserDetailsContextMapper;

import java.util.*;

public class PortalUserDetailsService implements UserDetailsContextMapper {

    private static final Log log = LogFactory.getLog(PortalUserDetailsService.class);

    // ref to our user dao
    private final PortalUserDAO portalUserDAO;

    /**
     * Constructor.
     *
     * Takes a ref to PortalUserDAO used to authenticate registered users in the
     * database.
     *
     * @param portalUserDAO PortalUserDAO
     */
    public PortalUserDetailsService(PortalUserDAO portalUserDAO) {
        this.portalUserDAO = portalUserDAO;
    }

    @Override
    public void mapUserToContext(UserDetails user, DirContextAdapter ctx)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public UserDetails mapUserFromContext(DirContextOperations ctx, String username, Collection<? extends GrantedAuthority> authority)
    {
        if (log.isDebugEnabled()) {
            log.debug("loadUserByUsername(), attempting to fetch portal user, email: " + username);
        }

        PortalUserDetails toReturn = null;
        User user = null;
        try {
            user = portalUserDAO.getPortalUser(username);
        } catch (Exception e ){
            log.debug("User " +username +" was not found in the cbio users table");
        }
        if (user != null && user.isEnabled()) {
            if (log.isDebugEnabled()) {
                log.debug("loadUserByUsername(), attempting to fetch portal user authorities, username: " + username);
            }
            UserAuthorities authorities = portalUserDAO.getPortalUserAuthorities(username);
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
            throw new UsernameNotFoundException("Error:  Unknown user or account disabled");
        }    
        else {
            if (log.isDebugEnabled()) {
                log.debug("loadUserByUsername(), successfully authenticated user, user name: " + username);
            }
            return toReturn;
        }
    }
}
