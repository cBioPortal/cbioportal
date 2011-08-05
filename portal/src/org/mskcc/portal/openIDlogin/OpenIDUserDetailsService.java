// package
package org.mskcc.portal.openIDlogin;

// imports
import org.mskcc.portal.util.Config;
import org.mskcc.portal.oauth.OAuthClient;

import org.springframework.security.openid.OpenIDAttribute;
import org.springframework.security.openid.OpenIDAuthenticationToken;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.core.authority.AuthorityUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.net.URL;
import java.util.List;


/**
 * Custom UserDetailsService which authenticates
 * OpenID user against backend cgds database via OAuth.
 *
 * @author Benjamin Gross
 */
public class OpenIDUserDetailsService
	implements UserDetailsService, AuthenticationUserDetailsService<OpenIDAuthenticationToken> {

	// logger
	private static Log log = LogFactory.getLog(OpenIDUserDetailsService.class);

	// ref to our OAuthConsumer
    private OAuthClient oauthClient;

	/**
	 * Constructor.
	 *
	 * @param oauthClient OAuthClient
	 */
	public OpenIDUserDetailsService(OAuthClient oauthClient) {
		this.oauthClient = oauthClient;
	}

    /**
     * Implementation of {@code UserDetailsService}.
	 * We only need this to satisfy the {@code RememberMeServices} requirements.
     */
    public UserDetails loadUserByUsername(String id) throws UsernameNotFoundException {
		throw new UnsupportedOperationException();
    }


    /**
     * Implementation of {@code AuthenticationUserDetailsService}
	 * which allows full access to the submitted {@code Authentication} object.
	 * Used by the OpenIDAuthenticationProvider.
     */
    public UserDetails loadUserDetails(OpenIDAuthenticationToken token) throws UsernameNotFoundException {

		// what we return
		OpenIDUserDetails toReturn = null;

		// get open id
        String id = token.getIdentityUrl();

		// grab other open id attributes
        String email = null;
        String firstName = null;
        String lastName = null;
        String fullName = null;
        List<OpenIDAttribute> attributes = token.getAttributes();
        for (OpenIDAttribute attribute : attributes) {
            if (attribute.getName().equals("email")) {
                email = attribute.getValues().get(0);
            }
            if (attribute.getName().equals("firstname")) {
                firstName = attribute.getValues().get(0);
            }
            if (attribute.getName().equals("lastname")) {
                lastName = attribute.getValues().get(0);
            }
            if (attribute.getName().equals("fullname")) {
                fullName = attribute.getValues().get(0);
            }
        }
        if (fullName == null) {
            StringBuilder fullNameBldr = new StringBuilder();
            if (firstName != null) {
                fullNameBldr.append(firstName);
            }
            if (lastName != null) {
                fullNameBldr.append(" ").append(lastName);
            }
            fullName = fullNameBldr.toString();
        }

		// check if this user exists in our backend db
		try {
			URL resourceURL =
				new URL(Config.getInstance().getProperty("cgds_credentials.url") + "?email_address=" + email);
			toReturn = extractUser(id, oauthClient.readProtectedResource(resourceURL));
			if (toReturn != null) {
				toReturn.setEmail(email);
				toReturn.setName(fullName);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		// outta here
		if (toReturn == null) {
			throw new UsernameNotFoundException("Error:  Unknown user or account disabled");
		}
		else {
			return toReturn;
		}
    }

	/**
	 * Helper function to convert unformatted protectedResource
	 * into an OpenIDUserDetails object.
	 *
	 * @param id String
	 * @param content String
	 */
    private OpenIDUserDetails extractUser(String id, String content) {

		// what we will return
		OpenIDUserDetails toReturn = null;

		// check for error/bad credentials
		if (content == null || content.startsWith("Error:")) {
			return null;
		}

		if (log.isDebugEnabled()) {
			log.debug("id: " + id);
		}
		
		String lines[] = content.split("\n");
		// first line is cgds server header, second is what we are after
		if (lines.length > 1) {
		
			// process: consumer-key\tconsumer-secret\tauthorites
			String parts[] = lines[1].split("\t");
			String consumerKey = parts[0];
			String consumerSecret = parts[1];
			String authorities[] = parts[2].split(",");

			// some logging
			if (log.isDebugEnabled()) {
				log.debug("consumerKey: " + consumerKey);
				log.debug("consumerSecret: " + consumerSecret);
				for (String authority : authorities) {
					log.debug("authority: " + authority);
				}
			}

			// create the OpenIDUserDetails object
			List<GrantedAuthority> grantedAuthorities =
				AuthorityUtils.createAuthorityList(authorities);
			toReturn  = new OpenIDUserDetails(id, grantedAuthorities);
			toReturn.setConsumerKey(consumerKey);
			toReturn.setConsumerSecret(consumerSecret);
		}

		// outta here
		return toReturn;
    }
}
