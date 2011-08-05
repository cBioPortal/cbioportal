// package
package org.mskcc.portal.oauth;

// imports
import java.net.URL;
import java.io.IOException;
import org.apache.commons.httpclient.NameValuePair;
import org.springframework.security.oauth.consumer.OAuthRequestFailedException;

/**
 * Our own OAuth Client interface.
 *
 * @author Benjamin Gross
 */
public interface OAuthClient {

	/**
	 * Reads a protected resource from the URL
	 * specified by resourceURL and data. It's 
	 * really a convenience method for the 
	 * method below.
	 *
	 * @param resourceURL String
	 * @param data NameValuePair[]
	 * @throws OAuthRequestFailedException
	 * @throws IOException
	 */
	public String readProtectedResource(String resourceURL, NameValuePair[] data)
		throws OAuthRequestFailedException, IOException;

	/**
	 * Reads a protected resource from the given URL.
	 *
	 * @param cgdsURL String
	 * @throws OAuthRequestFailedException
	 * @throws IOException
	 */
	public String readProtectedResource(URL resourceURL) 
		throws OAuthRequestFailedException, IOException;
}