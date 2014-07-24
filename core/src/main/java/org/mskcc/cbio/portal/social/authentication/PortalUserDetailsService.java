/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.portal.social.authentication;

import com.google.common.base.Strings;
import com.google.inject.internal.Preconditions;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mskcc.cbio.portal.dao.PortalUserDAO;
import org.mskcc.cbio.portal.model.User;
import org.mskcc.cbio.portal.model.UserAuthorities;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

/**
 * Responsible for verifying that a social site user name has been registered in the
 * portal database For registered users, an instance of GoogleplusUserDetails is
 * completed and returned. Null is returned for non-registered users
 *
 * Implementation based on code in OpenIDUserDetailsService
 *
 * @author criscuof
 */
public class PortalUserDetailsService implements UserDetailsService {

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
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(username), "A username is required");
        if (log.isDebugEnabled()) {
            log.debug("loadUserByUsername(), attempting to fetch portal user, email: " + username);
        }
        SocialUserDetails toReturn = null;
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
                toReturn = new SocialUserDetails(username, grantedAuthorities);
                toReturn.setEmail(user.getEmail());
                toReturn.setName(user.getName());
             
                
            }
        }

    // outta here
    if (toReturn == null) {
           
        log.debug("loadUserByUsername(), user and/or user authorities is null, user name: " +username);
        
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
