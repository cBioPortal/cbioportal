// package
package org.mskcc.portal.oauth.internal;

// imports
import org.mskcc.portal.oauth.OAuthClient;
import org.mskcc.portal.util.ResponseUtil;
import org.mskcc.portal.openIDlogin.OpenIDUserDetails;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.security.oauth.consumer.OAuthConsumerSupport;
import org.springframework.security.oauth.consumer.OAuthRequestFailedException;
import org.springframework.security.oauth.consumer.CoreOAuthConsumerSupport;
import org.springframework.security.oauth.consumer.token.OAuthConsumerToken;

// the following oauth.* used for debugging
import org.springframework.security.oauth.consumer.ProtectedResourceDetails;
import org.springframework.security.oauth.consumer.BaseProtectedResourceDetails;
import org.springframework.security.oauth.consumer.ProtectedResourceDetailsService;
import org.springframework.security.oauth.consumer.InMemoryProtectedResourceDetailsService;
import org.springframework.security.oauth.consumer.net.DefaultOAuthURLStreamHandlerFactory;
import org.springframework.security.oauth.common.signature.SignatureSecret;
import org.springframework.security.oauth.common.signature.SharedConsumerSecret;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.commons.httpclient.NameValuePair;

import java.net.URL;
import java.util.Map;
import java.io.IOException;

/**
 * This class implements org.mskcc.portal.oauth.OAuthClient which
 * speaks 2-Legged OAuth.
 *
 * See http://static.springsource.org/spring-security/oauth/twolegged.html.
 *
 * It can be passed an OAuthConsumer which describes the protected resource
 * or roll one using credentials stored in the spring-security context.
 *
 * If one is rolled, the class assumes the principal object stored in Authentication
 * is of type OpenIDUserDetails.
 *
 * @author Benjamin Gross
 */
public class TwoLeggedOAuthClientImpl implements OAuthClient {

	// some statics
	private static final String CGDS_RESOURCE = "CGDS-USER";
	private static final String SIGNATURE_METHOD = "HMAC-SHA1";
	private static Log log = LogFactory.getLog(TwoLeggedOAuthClientImpl.class);

	// an instance of this class
	// works on behalf of the following consumer support member
    private OAuthConsumerSupport consumerSupport;

	// even though we don't technically need an access token because we're using 2-legged oauth,
	// we still need to set the resource id of the token so Spring's OAuth Consumer can tell which protected
	// resource we're trying to connect to.  We will set this internal on behalf of the user.
	private String resourceID;

	/**
	 * Constructor.
	 * 
	 * We roll our own instance of OAuthConsumerSupport
	 * using the authentication-security context.
	 */
	public TwoLeggedOAuthClientImpl() {
		this.consumerSupport = rollOurOwnConsumer();
	}

	/**
	 * Constructor.
	 *
	 * @param consumerSupport OAuthConsumerSupport
	 * @param resourceID String
	 *
	 * OAuthConsumerSupport instance is provided.  Assumed to be a bean
	 * described in a context file.  The resourceID indicates which
	 * resource within the OAuthConsumerSupport to use for subsequent
	 * method calls.
	 */
	public TwoLeggedOAuthClientImpl(OAuthConsumerSupport consumerSupport, String resourceID) {
		this.consumerSupport = consumerSupport;
		this.resourceID = resourceID;
	}

	/**
	 * Roll our own consumer.  In most cases this will be used if 
	 * a ConsumerSupport bean was not defined in a context file.
	 * As mentioned in the class definition, this class assumes that
	 * the principal object stored within the authentication - security context
	 * is of type OpenIDUserDetails.
	 */
	private OAuthConsumerSupport rollOurOwnConsumer() {

		// what we are returning
		CoreOAuthConsumerSupport toReturn = null;

		// get key, secret from authentication object
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication.getPrincipal() instanceof OpenIDUserDetails) {
			OpenIDUserDetails openIDUserDetails = (OpenIDUserDetails)authentication.getPrincipal();
			final String consumerKey = openIDUserDetails.getEmail();
			final String consumerSecret = openIDUserDetails.getConsumerSecret();

			if (log.isDebugEnabled()) {
				log.debug("rollOurOwnConsumer: " + consumerKey + ", " + consumerSecret);
			}

			// create a new oauth consumer
			toReturn = new CoreOAuthConsumerSupport();
			toReturn.setProtectedResourceDetailsService(new ProtectedResourceDetailsService() {
				@Override
				public ProtectedResourceDetails loadProtectedResourceDetailsById(String id) throws IllegalArgumentException {
					BaseProtectedResourceDetails result = new BaseProtectedResourceDetails();
					result.setId(CGDS_RESOURCE);
					result.setConsumerKey(consumerKey);
					result.setSharedSecret(new SharedConsumerSecret(consumerSecret));
					result.setSignatureMethod(SIGNATURE_METHOD);
					return result;
				}
			});
			// this should match argument to result.setId above
			this.resourceID = CGDS_RESOURCE;
		}

		// outta here
        return toReturn;
    }

	/**
	 * Implementation of {@code OAuthClient}.
	 */
	public String readProtectedResource(String resourceURL, NameValuePair[] data)
		throws OAuthRequestFailedException, IOException {
		
		// construct url with name - value pairs
		StringBuffer canonicalURL = new StringBuffer(resourceURL + "?");
        for (NameValuePair nvp: data) {
			String name = nvp.getName().trim();
			String value = nvp.getValue().trim().replaceAll(" ", "+");
			canonicalURL.append("&" + name + "=" + value);
        }
		canonicalURL.deleteCharAt(canonicalURL.indexOf("&"));
		return readProtectedResource(new URL(canonicalURL.toString()));
	}

	/**
	 * Implementation of {@code OAuthClient}.
	 */
	public String readProtectedResource(URL resourceURL) 
		throws OAuthRequestFailedException, IOException {

		// check that we have a valid consumerSupport instance
		if (this.consumerSupport == null) {
			throw new OAuthRequestFailedException("ConsumerSupport Object is null.");
		}
	
		if (log.isDebugEnabled()) {
			writeToLog(resourceURL);
		}

		// the following is important when our
		// OAuthConsumerSupport is described in a bean definition file
        OAuthConsumerToken accessToken = new OAuthConsumerToken();
        accessToken.setResourceId(resourceID);

		// outta here
        return ResponseUtil.getResponseString(consumerSupport.readProtectedResource(resourceURL, accessToken, "GET"));
    }

	/**
	 * A helper method to log (debug) OAuth Consumer settings.
	 */
	private void writeToLog(URL resourceURL) {

		log.debug("resourceURL: " + resourceURL);
		log.debug("resourceID: " + resourceID);
		ProtectedResourceDetails protectedResourceDetails = null;
		ProtectedResourceDetailsService protectedResourceDetailsService =
			((CoreOAuthConsumerSupport)consumerSupport).getProtectedResourceDetailsService();
		if (protectedResourceDetailsService instanceof InMemoryProtectedResourceDetailsService) {
			Map<String, ? extends ProtectedResourceDetails> resourceDetailsStore = 
				((InMemoryProtectedResourceDetailsService)protectedResourceDetailsService).getResourceDetailsStore();
			protectedResourceDetails = resourceDetailsStore.get(resourceID);
		}
		else {
			protectedResourceDetails =
				protectedResourceDetailsService.loadProtectedResourceDetailsById(resourceID);
		}
		log.debug("consumer key: " + protectedResourceDetails.getConsumerKey());
		log.debug("signature method: " + protectedResourceDetails.getSignatureMethod());
		SignatureSecret sharedSecret = protectedResourceDetails.getSharedSecret();
		log.debug("shared secret: " + ((SharedConsumerSecret)sharedSecret).getConsumerSecret());
	}
}