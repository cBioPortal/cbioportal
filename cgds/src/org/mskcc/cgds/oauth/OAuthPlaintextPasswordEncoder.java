// package
package org.mskcc.cgds.oauth;

// imports
import org.springframework.security.authentication.encoding.PasswordEncoder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class implements a PasswordEncoder interface which
 * allows the use of plaintext passwords during an OAuth exchange.
 *
 * We are ok with plaintext passwords since the communication channel
 * with CGDS will be encrypted (HTTPS).
 *
 * Note, by default CoreOAuthSignatureMethodFactory does not 
 * support PLAINTEXT signatures.  A call to setSupportPlainText(true)
 * is required.
 *
 * Also note, CoreOAuthSignatureMethodyFactory assumes that a tokenSecret
 * is being used (not the case with 2-Legged), so it appends a '&' followed
 * by the empty token secret which results in a password word with a '&'
 * apppended.  I've modified the class in spring-security-oauth to prevent
 * that from happening.
 *
 * @author Benjamin Gross
 */
public class OAuthPlaintextPasswordEncoder implements PasswordEncoder {

	// ref to our logger
	private static Log log = LogFactory.getLog(OAuthPlaintextPasswordEncoder.class);

	/**
	 * Implementation of {@code PasswordEncoder}.
	 */
	public String encodePassword(String rawPass, Object salt) {

		if (log.isDebugEnabled()) {
			log.debug("encodePassword(), rawPass: " + rawPass);
		}
		return rawPass;
	}

	/**
	 * Implementation of {@code PasswordEncoder}.
	 */
	public boolean isPasswordValid(String encPass, String rawPass, Object salt) {
        
		if (log.isDebugEnabled()) {
			log.debug("isPasswordValid(), encPass: " + encPass);
			log.debug("isPasswordValid(), rawPass: " + rawPass);
		}

        // need to check password with encrypted version in the db
		
		// outta here
		return true;
	}
}
