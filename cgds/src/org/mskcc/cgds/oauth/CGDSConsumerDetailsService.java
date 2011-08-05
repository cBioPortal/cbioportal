// package
package org.mskcc.cgds.oauth;

// imports
import org.mskcc.cgds.model.User;
import org.mskcc.cgds.model.UserAuthorities;
import org.mskcc.cgds.dao.DaoUser;
import org.mskcc.cgds.dao.DaoUserAuthorities;
import org.mskcc.cgds.dao.DaoException;

import org.springframework.security.oauth.provider.ConsumerDetails;
import org.springframework.security.oauth.provider.BaseConsumerDetails;
import org.springframework.security.oauth.provider.ConsumerDetailsService;
import org.springframework.security.oauth.common.OAuthException;
import org.springframework.security.oauth.common.signature.SignatureSecret;
import org.springframework.security.oauth.common.signature.SharedConsumerSecret;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Map;
import java.util.List;
import java.util.HashMap;
import java.util.Collection;

/**
 * This is our OAUth consumer service provider.
 *
 * @author Benjamin Gross
 */
public class CGDSConsumerDetailsService implements ConsumerDetailsService, UserDetailsService {
 
	// ref to hashmap of consumers
    private Map<String, ConsumerDetails> consumers;

	// logger
	private static Log log = LogFactory.getLog(CGDSConsumerDetailsService.class);

	/**
	 * Constructor.
	 */
    public CGDSConsumerDetailsService() throws DaoException {

		// at startup, read users from db
		initializeConsumerMap();
    }

	/**
	 * Our OAuth consumer service endpoint.
	 *
	 * @param key String
	 */
    @Override
    public ConsumerDetails loadConsumerByConsumerKey(String key) throws OAuthException {

		if (log.isDebugEnabled()) {
			log.debug("key: " + key);
		}

        ConsumerDetails consumer = consumers.get(key);

        if (consumer == null) {
			log.debug("consumer is null (cannot be found in map), throwing OAuthException.");
            throw new OAuthException("No consumer found for key " + key);
        }

		if (log.isDebugEnabled()) {
			log.debug("consumer key: " + consumer.getConsumerKey());
			log.debug("consumer name: " + consumer.getConsumerName());
			SignatureSecret sharedSecret = consumer.getSignatureSecret();
			log.debug("shared secret: " + ((SharedConsumerSecret)sharedSecret).getConsumerSecret());
			List<GrantedAuthority> authorities = consumer.getAuthorities();
			for (GrantedAuthority authority : authorities) {
				log.debug(authority.getAuthority());
			}
		}

		// outta here
        return consumer;
    }
 
	/**
	 * We only provide OAuth access.
	 *
	 * @param username String
	 */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        throw new UnsupportedOperationException();
    }

	/**
	 * Helper method to populate our consumers hashmap.
	 * We may want to read from DB at runtime to allow updates
	 * to consumers dynamically.
	 */
	private void initializeConsumerMap() throws DaoException {

		// map to store consumer info
        consumers = new HashMap<String, ConsumerDetails>();

		// get all users from db
		for (User user : DaoUser.getAllUsers()) {
			// if user acct is enabled, put in map
			if (user.isEnabled()) {
				UserAuthorities authorities = DaoUserAuthorities.getUserAuthorities(user);
				consumers.put(user.getConsumerKey(),
							  createConsumerDetails(user.getConsumerKey(), user.getEmail(), user.getConsumerSecret(), authorities.getAuthorities()));
			}
		}
	}
 
	/**
	 * Helper method to create consumer details objects.
	 * 
	 * @param consumerKey String
	 * @param consumerName String
	 * @param consumerSecret String
	 * @param authorities Collection<String>
	 */
    private ConsumerDetails createConsumerDetails(String consumerKey, String consumerName, String consumerSecret, Collection<String> authorities) {

        SharedConsumerSecret secret = new SharedConsumerSecret(consumerSecret);

		if (log.isDebugEnabled()) {
			log.debug("consumer key: " + consumerKey);
			log.debug("consumer name: " + consumerName);
			log.debug("consumer secret: " + consumerSecret);
			for (String authority : authorities) {
				log.debug("authority: " + authority);
			}
		}
 
        BaseConsumerDetails toReturn = new BaseConsumerDetails();
        toReturn.setConsumerKey(consumerKey);
        toReturn.setConsumerName(consumerName);
        toReturn.setSignatureSecret(secret);
		toReturn.setAuthorities(AuthorityUtils.createAuthorityList(authorities.toArray(new String[0])));
 
        // set this to false to enable the 2legged OAuth model
        // see http://spring-security-oauth.codehaus.org/twolegged.html
        toReturn.setRequiredToObtainAuthenticatedToken(false);
 
		// outta here
        return toReturn;
    }
 }
