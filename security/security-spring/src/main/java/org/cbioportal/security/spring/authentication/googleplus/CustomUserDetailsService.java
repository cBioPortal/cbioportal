package org.cbioportal.security.spring.authentication.googleplus;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.social.security.SocialUser;
import org.springframework.social.security.SocialUserDetailsService;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

public class CustomUserDetailsService implements SocialUserDetailsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomUserDetailsService.class);
    
    /**
     * Loads the username by using the account ID of the user.
     * @param userId    The account ID of the requested user.
     * @return  The information of the requested user.
     */

    @Override
    public org.springframework.social.security.SocialUserDetails loadUserByUserId(String userId) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(userId), "A userid is required");
        LOGGER.debug("Loading user by user id: {}", userId);
        
        return new SocialUser(userId,  "unused", new ArrayList<GrantedAuthority>());
        
    }
}
