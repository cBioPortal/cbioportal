package org.cbioportal.security.spring.authentication.token.config;

import org.cbioportal.security.spring.authentication.social.PortalUserDetailsService;
import org.cbioportal.security.spring.authentication.token.TokenUserDetailsAuthenticationProvider;
import org.cbioportal.security.spring.authentication.token.oauth2.OAuth2DataAccessTokenServiceImpl;
import org.cbioportal.security.spring.authentication.token.oauth2.OAuth2TokenAuthenticationProvider;
import org.cbioportal.service.impl.JwtDataAccessTokenServiceImpl;
import org.cbioportal.service.impl.UnauthDataAccessTokenServiceImpl;
import org.cbioportal.service.impl.UuidDataAccessTokenServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;

@Configuration
@PropertySources({
        @PropertySource(value="classpath:portal.properties", ignoreResourceNotFound=true),
        @PropertySource(value="file:///${PORTAL_HOME}/portal.properties", ignoreResourceNotFound=true)
})
public class DataAccessTokenConfig {

    // provider
    @Bean("tokenAuthenticationProvider")
    @ConditionalOnDatMethod(value = "oauth2")
    public OAuth2TokenAuthenticationProvider oauth2TokenAuthenticationProvider() {
        return new OAuth2TokenAuthenticationProvider();
    }

    @Bean("tokenAuthenticationProvider")
    @ConditionalOnDatMethod(value = "oauth2", isNot = true)
    public TokenUserDetailsAuthenticationProvider userDetailsTokenAuthenticationProvider() {
        return new TokenUserDetailsAuthenticationProvider(tokenUserDetailsService());
    }

    @Bean
    @ConditionalOnDatMethod(value = "oauth2", isNot = true)
    public PortalUserDetailsService tokenUserDetailsService() {
        return new PortalUserDetailsService();
    }


    // service
    @Bean("dataAccessTokenService")
    @ConditionalOnDatMethod(value = "oauth2")
    public OAuth2DataAccessTokenServiceImpl oauth2DataAccessTokenService() {
        return new OAuth2DataAccessTokenServiceImpl();
    }

    @Bean("dataAccessTokenService")
    @ConditionalOnDatMethod(value = "none")
    public UnauthDataAccessTokenServiceImpl unauthDataAccessTokenService() {
        return new UnauthDataAccessTokenServiceImpl();
    }

    @Bean("dataAccessTokenService")
    @ConditionalOnDatMethod(value = "uuid")
    public UuidDataAccessTokenServiceImpl uuidDataAccessTokenService() {
        return new UuidDataAccessTokenServiceImpl();
    }

    @Bean("dataAccessTokenService")
    @ConditionalOnDatMethod(value = "jwt")
    public JwtDataAccessTokenServiceImpl jwtDataAccessTokenService() {
        return new JwtDataAccessTokenServiceImpl();
    }

}