package org.cbioportal.security.spring.authentication.token.oauth2;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@ContextConfiguration(classes = OAuth2TokenRefreshTemplateTestConfiguration.class)
@RunWith(SpringRunner.class)
public class OAuth2TokenRefreshTemplateTest {

	public static String ACCESS_TOKEN = "dummy_access_token";
    public static String OFFLINE_TOKEN_VALID = "dummy_valid_offline_token";
    public static String OFFLINE_TOKEN_INVALID = "dummy_invalid_offline_token";

    @Autowired
    private OAuth2TokenRefreshRestTemplate tokenRefreshTemplate;
    
    @Test
    public void testGetAccessTokenSuccess() {
        String accessToken = tokenRefreshTemplate.getAccessToken(OFFLINE_TOKEN_VALID);
        assertEquals(accessToken, ACCESS_TOKEN);
    }
    
    @Test(expected = BadCredentialsException.class)
    public void testGetAccessTokenFailure() {
        tokenRefreshTemplate.getAccessToken(OFFLINE_TOKEN_INVALID);
    }

}
