package org.cbioportal.security.spring.authentication.token.oauth2;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;

import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;

@Configuration
public class OAuth2TokenRefreshTemplateTestConfiguration {

    @Bean
    public OAuth2TokenRefreshRestTemplate tokenRefreshTemplate() {
        return new OAuth2TokenRefreshRestTemplate();
    }

    @Bean
    public RestTemplate template() {
        final RestTemplate template = mock(RestTemplate.class);

        final String jsonSuccess = "{\"access_token\":\"" + OAuth2TokenRefreshTemplateTest.ACCESS_TOKEN + "\"}";
        final ResponseEntity<String> responseSuccess = new ResponseEntity<String>(jsonSuccess, HttpStatus.OK);

        final String jsonFailure = "{\"error\":\"invalid_grant\"}";
        final ResponseEntity<String> responseFailure = new ResponseEntity<String>(jsonFailure, HttpStatus.UNAUTHORIZED);

        Mockito.doAnswer(invocation -> {
            @SuppressWarnings("unchecked")
			final
            HttpEntity<LinkedMultiValueMap<String, Object>> httpEntity = invocation.getArgumentAt(1, HttpEntity.class);
            final String code = (String) httpEntity.getBody().get("refresh_token").get(0);
            if (code.equals(OAuth2TokenRefreshTemplateTest.OFFLINE_TOKEN_VALID)) {
                return responseSuccess;
            }
            return responseFailure;
        }).when(template).postForEntity(anyString(), any(HttpEntity.class), eq(String.class));

        return template;
    }

}
