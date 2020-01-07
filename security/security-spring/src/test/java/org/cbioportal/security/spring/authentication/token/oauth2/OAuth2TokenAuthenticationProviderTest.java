package org.cbioportal.security.spring.authentication.token.oauth2;

import static org.junit.Assert.assertArrayEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@ContextConfiguration(classes = OAuth2TokenAuthenticationProviderTestConfiguration.class)
@RunWith(SpringRunner.class)
public class OAuth2TokenAuthenticationProviderTest {

    public static String USER_ROLE_1 = "dummy_role_1";
    public static String USER_ROLE_2 = "dummy_role_2";
    public static String OFFLINE_TOKEN_VALID = "my_valid_offline_token";
    public static String OFFLINE_TOKEN_MALFORMED_JSON = "dummy_malformed_json_response";

    @Autowired
    private OAuth2TokenAuthenticationProvider provider;

    @Test
    public void testAuthenticateSuccess() {

        Authentication authRequest = new OAuth2BearerAuthenticationToken("my_principal", OFFLINE_TOKEN_VALID);
        Authentication authGranted = provider.authenticate(authRequest);

        String[] authorities = authGranted.getAuthorities().stream()
            .map(a -> a.toString())
            .toArray(String[]::new);

        assertArrayEquals(authorities, new String[] {USER_ROLE_1, USER_ROLE_2});
    }

    @Test(expected = BadCredentialsException.class)
    public void testAuthenticateFailureMalformedJson() {
        Authentication authRequest = new OAuth2BearerAuthenticationToken("my_principal", OFFLINE_TOKEN_MALFORMED_JSON);
        provider.authenticate(authRequest);
    }

}