// package
package org.mskcc.cgds.oauth;

// imports
import org.springframework.security.oauth.provider.ConsumerAuthentication;
import org.springframework.security.oauth.provider.OAuthAuthenticationHandler;
import org.springframework.security.oauth.provider.token.OAuthAccessProviderToken;
import org.springframework.security.core.Authentication;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * We implement this interface to convert the consumer authentication
 * (which is handled by the OAuth plugin during the request/authorize/access token exchange)
 *  in an user authentication, which is required by Spring Security before
 * allowing any client to access the protected resources.
 *
 * @author Benjamin Gross
 */
public class ConsumerBasedAuthenticationHandler implements OAuthAuthenticationHandler {
	
	private static Log log = LogFactory.getLog(ConsumerBasedAuthenticationHandler.class);
 
    @Override
    public Authentication createAuthentication(HttpServletRequest request, ConsumerAuthentication authentication, OAuthAccessProviderToken authToken) {

		if (log.isDebugEnabled()) {
			log.debug("authentication name: " + authentication.getName());
		}
        // return the consumer authentication to replace the user authentication
        return authentication;
    }
}