package org.cbioportal.security.spring.authentication.live;

import org.springframework.social.connect.UserProfile;
import org.springframework.social.connect.support.OAuth2ConnectionFactory;
import org.springframework.social.live.api.Live;
import org.springframework.social.live.connect.LiveAdapter;
import org.springframework.social.live.connect.LiveServiceProvider;
import org.springframework.social.oauth2.AccessGrant;

public class LiveConnectionFactory extends OAuth2ConnectionFactory<Live> {

    public LiveConnectionFactory(String clientId, String clientSecret) {
        super("live", new LiveServiceProvider(clientId, clientSecret), new LiveAdapter());
    }

    /*
     * modification of original factory class to support using the user's email
     * address as his/her id original method utilized the google id, a numeric
     * string
     */
    @Override
    protected String extractProviderUserId(AccessGrant accessGrant) {
        Live live = ((LiveServiceProvider) getServiceProvider()).getApi(accessGrant.getAccessToken());
        UserProfile userProfile = getApiAdapter().fetchUserProfile(live);
        return userProfile.getEmail();
    }

}