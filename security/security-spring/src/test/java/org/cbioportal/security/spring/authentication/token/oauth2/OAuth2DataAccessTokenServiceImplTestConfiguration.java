package org.cbioportal.security.spring.authentication.token.oauth2;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;

import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.jwt.crypto.sign.RsaVerifier;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;

@Configuration
public class OAuth2DataAccessTokenServiceImplTestConfiguration {

    @Bean
    public OAuth2DataAccessTokenServiceImpl dataAccessTokenService() {
        return new OAuth2DataAccessTokenServiceImpl();
    }

    @Bean
    public RestTemplate template() {
        RestTemplate template = mock(RestTemplate.class);

        String jsonSuccess = "{\"refresh_token\":\"" + OAuth2DataAccessTokenServiceImplTest.ACCESS_TOKEN + "\"}";
        ResponseEntity<String> responseSuccess = new ResponseEntity<String>(jsonSuccess, HttpStatus.OK);

        String jsonFailure = "{\"error\":\"invalid_grant\"}";
        ResponseEntity<String> responseFailure = new ResponseEntity<String>(jsonFailure, HttpStatus.UNAUTHORIZED);

        Mockito.doAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            HttpEntity<LinkedMultiValueMap<String, Object>> httpEntity = invocation.getArgumentAt(1, HttpEntity.class);
            String code = (String) httpEntity.getBody().get("code").get(0);
            if (code.equals(OAuth2DataAccessTokenServiceImplTest.ACCESS_CODE_VALID)) {
                return responseSuccess;
            }
            return responseFailure;
        }).when(template).postForEntity(anyString(), any(HttpEntity.class), eq(String.class));

        return template;
    }

    @Bean
    public JwtTokenVerifierBuilder jwtTokenVerifierBuilder() {

        // create a verifier that always returns true,
        // i.e., does not throw an exception.
        RsaVerifier verifier = mock(RsaVerifier.class);
        doNothing().when(verifier).verify(any(), any());

        // create a builder that returns the verifier
        JwtTokenVerifierBuilder builder = mock(JwtTokenVerifierBuilder.class);
        try {
            Mockito.when(builder.build(anyString())).thenReturn(verifier);
        } catch (Exception e) {}

        return builder;
    }

}
