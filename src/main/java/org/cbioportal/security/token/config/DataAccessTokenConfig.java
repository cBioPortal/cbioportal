package org.cbioportal.security.token.config;

import org.cbioportal.security.token.oauth2.OAuth2DataAccessTokenServiceImpl;
import org.cbioportal.security.token.oauth2.OAuth2TokenAuthenticationProvider;
import org.cbioportal.service.impl.UnauthDataAccessTokenServiceImpl;
import org.cbioportal.utils.config.annotation.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "dat.method", havingValue = {"", "none"}, isNot = true)
public class DataAccessTokenConfig {

    // provider
    @Bean("tokenAuthenticationProvider")
    @ConditionalOnProperty(name = "dat.method", havingValue = "oauth2")
    public OAuth2TokenAuthenticationProvider oauth2TokenAuthenticationProvider() {
        return new OAuth2TokenAuthenticationProvider();
    }
    
    // TODO - implement uuid and jwt providers
//    @Bean("tokenAuthenticationProvider")
//    @ConditionalOnProperty(name = "dat.method", havingValue = "oauth2", isNot = true)
//    public TokenUserDetailsAuthenticationProvider userDetailsTokenAuthenticationProvider() {
//        return new TokenUserDetailsAuthenticationProvider(tokenUserDetailsService());
//    }

//    @Bean
//    @ConditionalOnProperty(name = "dat.method", havingValue = "oauth2", isNot = true)
//    public PortalUserDetailsService tokenUserDetailsService() {
//        return new PortalUserDetailsService();
//    }

    // service
    @Bean("dataAccessTokenService")
    @ConditionalOnProperty(name = "dat.method", havingValue = "oauth2")
    public OAuth2DataAccessTokenServiceImpl oauth2DataAccessTokenService() {
        return new OAuth2DataAccessTokenServiceImpl();
    }

    @Bean("dataAccessTokenService")
    @ConditionalOnProperty(name = "dat.method", havingValue = "none")
    public UnauthDataAccessTokenServiceImpl unauthDataAccessTokenService() {
        return new UnauthDataAccessTokenServiceImpl();
    }

    // TODO - implement uuid and jwt providers
//    @Bean("dataAccessTokenService")
//    @ConditionalOnProperty(name = "dat.method", havingValue = "uuid")
//    public UuidDataAccessTokenServiceImpl uuidDataAccessTokenService() {
//        return new UuidDataAccessTokenServiceImpl();
//    }

//    @Bean("dataAccessTokenService")
//    @ConditionalOnProperty(name = "dat.method", havingValue = "jwt")
//    public JwtDataAccessTokenServiceImpl jwtDataAccessTokenService() {
//        return new JwtDataAccessTokenServiceImpl();
//    }

}