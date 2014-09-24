
package org.mskcc.cbio.portal.socialUser;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import java.util.ArrayList;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mskcc.cbio.portal.dao.PortalUserDAO;
import org.mskcc.cbio.portal.model.User;
import org.mskcc.cbio.portal.model.UserAuthorities;
import org.springframework.dao.DataAccessException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.GrantedAuthorityImpl;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.social.security.SocialUser;
import org.springframework.social.security.SocialUserDetailsService;

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
public class PortalSocialUserDetailsService implements UserDetailsService, SocialUserDetailsService  {

    private final PortalUserDAO portalUserDao;
    private static final Log log = LogFactory.getLog(PortalSocialUserDetailsService.class);
    private static final String DEFAULT_PASSWORD ="";  
    private static final Boolean ACCOUNT_NON_EXPIRED = Boolean.TRUE;
    private static final Boolean CREDENTIAL_NON_EXPIRED = Boolean.TRUE;
    private static final Boolean ACCOUNT_NON_LOCKED = Boolean.TRUE;
    
   
    public PortalSocialUserDetailsService(PortalUserDAO dao){
        this.portalUserDao = dao;
    }
    
    
  @Override
	public UserDetails loadUserByUsername(String username)
			throws UsernameNotFoundException {
		Preconditions.checkArgument(!Strings.isNullOrEmpty(username), "A username is required");
		log.info("loadUserByUsername invoked for " +username);
		
		SocialUser user = convertToSocialUser(this.portalUserDao.getPortalUser(username));
		if (null != user && user.isEnabled()){
			log.info("Found enabled user: " +username +" in database");
			return user;
			} 
		log.info("User: " +username +" is either not registered or not enabled");
		
		return null;
	}
	

	@Override
	public org.springframework.social.security.SocialUserDetails loadUserByUserId(
			String userId) throws UsernameNotFoundException, DataAccessException {
		Preconditions.checkArgument(!Strings.isNullOrEmpty(userId),"A userid is required");
		log.info("+++ Getting user details for id: " +userId);
		SocialUser user = convertToSocialUser(this.portalUserDao.getPortalUser(userId));
		if (null != user && user.isEnabled()){
			log.info("Found enabled user:" +userId +" in database");
			return user;
			} 
		log.info("User: " +userId +" is either not registered or not enabled");
		
		return null;
	}

        
        /**
         * private method to convert a cbio model User object to a SocialUser object
         * includes a collection of GrantedAothority objects
         * @param user
         * @return 
         */
	private SocialUser convertToSocialUser(User user) {
            SocialUser su = new SocialUser(user.getEmail(),DEFAULT_PASSWORD,user.isEnabled(), ACCOUNT_NON_EXPIRED,CREDENTIAL_NON_EXPIRED,
                    ACCOUNT_NON_LOCKED, new ArrayList<GrantedAuthority>());
            UserAuthorities ua = this.portalUserDao.getPortalUserAuthorities(su.getUsername());
            for(String authority : ua.getAuthorities()){
                su.getAuthorities().add(new GrantedAuthorityImpl(authority));
            }
            return su;
        }

}
