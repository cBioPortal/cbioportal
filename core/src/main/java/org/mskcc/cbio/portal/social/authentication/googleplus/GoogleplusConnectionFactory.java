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
package org.mskcc.cbio.portal.social.authentication.googleplus;
import org.springframework.social.connect.UserProfile;
import org.springframework.social.connect.support.OAuth2ConnectionFactory;
import org.springframework.social.google.api.Google;
import org.springframework.social.google.connect.GoogleAdapter;
import org.springframework.social.google.connect.GoogleServiceProvider;
import org.springframework.social.oauth2.AccessGrant;
/**
 * @author criscuof
 *
 */
public class GoogleplusConnectionFactory extends OAuth2ConnectionFactory<Google> {
	


	public GoogleplusConnectionFactory(String clientId, String clientSecret) {
		super("google", new GoogleServiceProvider(clientId, clientSecret),
				new GoogleAdapter());
	}

	/*
	 * modification of original factory class to support using the user's email address as his/her id
	 * original method utilized the google id, a numeric string
	 */
	@Override
	protected String extractProviderUserId(AccessGrant accessGrant) {
		Google api = ((GoogleServiceProvider)getServiceProvider()).getApi(accessGrant.getAccessToken());
	    UserProfile userProfile = getApiAdapter().fetchUserProfile(api);
	    return userProfile.getEmail();
	}


}
