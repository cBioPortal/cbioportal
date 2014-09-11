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

package org.mskcc.cbio.portal.authentication.googleplus;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.social.security.SocialUser;
import org.springframework.social.security.SocialUserDetailsService;






public class GoogleplusUserDetailsService implements SocialUserDetailsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(GoogleplusUserDetailsService.class);

    private UserDetailsService userDetailsService;

    public GoogleplusUserDetailsService(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
        
    }

    /**
     * Loads the username by using the account ID of the user.
     * @param userId    The account ID of the requested user.
     * @return  The information of the requested user.
     * @throws UsernameNotFoundException    Thrown if no user is found.
     * @throws DataAccessException
     */
   

    @Override
    public org.springframework.social.security.SocialUserDetails loadUserByUserId(String userId) throws UsernameNotFoundException, DataAccessException {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(userId), "A userid is required");
        LOGGER.debug("Loading user by user id: {}", userId);

        UserDetails ud = userDetailsService.loadUserByUsername(userId);
        LOGGER.debug("Found user details: " +ud.getUsername());
        /**
         * Map Spring Security UserDetails implementation to a Spring Social SocialUser instance
         */
        return new SocialUser(ud.getUsername(), 
                ud.getPassword(),
                ud.isEnabled(),
                ud.isAccountNonExpired(),
                ud.isCredentialsNonExpired(),
                ud.isAccountNonLocked(),
                ud.getAuthorities());
                    
        
    }
    
  
}

