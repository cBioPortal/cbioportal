package org.cbioportal.security.spring;

import org.cbioportal.security.spring.authentication.token.TokenAuthenticationFilter;
import org.cbioportal.security.spring.authentication.token.TokenAuthenticationSuccessHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.context.SecurityContextPersistenceFilter;

@Configuration
@Order(SecurityProperties.BASIC_AUTH_ORDER - 2)
@ConditionalOnExpression("'${authenticate}' ne 'false' && '${authenticate}' ne 'noauthsessionservice'  && '${dat.method:none}' ne 'none'")
public class DatAccessApiSecurityConfig extends ApiSecurityConfig {

    @Autowired
    private AuthenticationProvider tokenAuthenticationProvider;

    @Autowired
    private TokenAuthenticationSuccessHandler tokenAuthenticationSuccessHandler;

    // Update the Spring Boot AuthenticationManager to contain a tokenAuthenticationProvider
    // (see: "Customizing Authentication Managers" @ https://spring.io/guides/topicals/spring-security-architecture
    @Autowired
    public void initialize(AuthenticationManagerBuilder builder) {
        if (tokenAuthenticationProvider != null) {
            builder.authenticationProvider(tokenAuthenticationProvider);
        }
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // Update HttpSecurity with parent class.
        super.configure(http);
        http
            .addFilterAfter(tokenAuthenticationFilter(), SecurityContextPersistenceFilter.class);
    }

    @Bean
    public TokenAuthenticationFilter tokenAuthenticationFilter() throws Exception {
        TokenAuthenticationFilter tokenAuthenticationFilter =
            new TokenAuthenticationFilter("/**", authenticationManager());
        tokenAuthenticationFilter.setAuthenticationSuccessHandler(
            tokenAuthenticationSuccessHandler);
        return tokenAuthenticationFilter;
    }

}
