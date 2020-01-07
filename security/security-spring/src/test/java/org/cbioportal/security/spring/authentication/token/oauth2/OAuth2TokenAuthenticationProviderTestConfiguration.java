package org.cbioportal.security.spring.authentication.token.oauth2;

import static org.cbioportal.security.spring.authentication.token.oauth2.OAuth2TokenTestUtils.createJwt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;

import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class OAuth2TokenAuthenticationProviderTestConfiguration {

    @Bean
    public OAuth2TokenAuthenticationProvider provider() {
        return new OAuth2TokenAuthenticationProvider();
    }

    @Bean
    public OAuth2TokenRefreshRestTemplate tokenRefreshRestTemplate() {

        String jsonSuccess = new StringBuilder()
            .append("{")
            .append("\"resource_access\":")
                .append("{")
                    .append("\"cbioportal\":")
                    .append("{")
                        .append("\"roles\": [")
                            .append("\"").append(OAuth2TokenAuthenticationProviderTest.USER_ROLE_1).append("\",")
                            .append("\"").append(OAuth2TokenAuthenticationProviderTest.USER_ROLE_2).append("\"")
                        .append("]")
                    .append("}")
                .append("}")
            .append("}")
            .toString();

            
        String accessTokenSuccess = createJwt(jsonSuccess);
        
        String jsonMalformedJson = new StringBuilder()
            .append("{")
            .append("\"resource_access\":")
                .append("{}")
            .append("}")
            .toString();

        String accessTokenMalformedJson = createJwt(jsonMalformedJson);

        OAuth2TokenRefreshRestTemplate template = mock(OAuth2TokenRefreshRestTemplate.class);
        Mockito.doAnswer(invocation -> {
            String offlineToken = invocation.getArgumentAt(0, String.class);
            if (offlineToken.equals(OAuth2TokenAuthenticationProviderTest.OFFLINE_TOKEN_MALFORMED_JSON)) {
                return accessTokenMalformedJson;
            }
            return accessTokenSuccess;
        }).when(template).getAccessToken(anyString());

        return template;
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

}