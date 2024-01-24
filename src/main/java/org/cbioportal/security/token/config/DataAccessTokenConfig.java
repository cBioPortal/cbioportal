package org.cbioportal.security.token.config;

import org.cbioportal.persistence.SecurityRepository;
import org.cbioportal.security.token.oauth2.JwtTokenVerifierBuilder;
import org.cbioportal.security.token.oauth2.OAuth2DataAccessTokenServiceImpl;
import org.cbioportal.security.token.oauth2.OAuth2TokenAuthenticationProvider;
import org.cbioportal.security.token.oauth2.OAuth2TokenRefreshRestTemplate;
import org.cbioportal.security.token.uuid.UuidTokenAuthenticationProvider;
import org.cbioportal.service.impl.UnauthDataAccessTokenServiceImpl;
import org.cbioportal.utils.config.annotation.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
@ConditionalOnProperty(name = "dat.method", havingValue = {"", "none"}, isNot = true)
public class DataAccessTokenConfig {

    
    // provider
    @Bean("tokenAuthenticationProvider")
    @ConditionalOnProperty(name = "dat.method", havingValue = "oauth2")
    public OAuth2TokenAuthenticationProvider oauth2TokenAuthenticationProvider(OAuth2TokenRefreshRestTemplate refreshRestTemplate) {
        return new OAuth2TokenAuthenticationProvider(refreshRestTemplate);
    }
    
    // TODO - implement jwt providers
    @Bean("tokenAuthenticationProvider")
    @ConditionalOnProperty(name = "dat.method", havingValue = "uuid")
    public UuidTokenAuthenticationProvider uuidTokenAuthenticationProvider(SecurityRepository repository) {
        return new UuidTokenAuthenticationProvider(repository);
    }

    // service
    @Bean("dataAccessTokenService")
    @ConditionalOnProperty(name = "dat.method", havingValue = "oauth2")
    public OAuth2DataAccessTokenServiceImpl oauth2DataAccessTokenService(RestTemplate template, JwtTokenVerifierBuilder jwtTokenVerifierBuilder) {
        return new OAuth2DataAccessTokenServiceImpl(template, jwtTokenVerifierBuilder);
    }

    @Bean("dataAccessTokenService")
    @ConditionalOnProperty(name = "dat.method", havingValue = "none")
    public UnauthDataAccessTokenServiceImpl unauthDataAccessTokenService() {
        return new UnauthDataAccessTokenServiceImpl();
    }

}