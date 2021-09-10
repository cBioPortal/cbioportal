package org.cbioportal.security.spring;

import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
@ConditionalOnExpression("'${authenticate}' ne 'false' && '${authenticate}' ne 'noauthsessionservice'")
@Order(Ordered.HIGHEST_PRECEDENCE)
public class UnprotectedResourcesSecurityConfig extends WebSecurityConfigurerAdapter {

    // Add security filter chains (note the plural) that handle paths to unprotected resources.
    // see: "Creating and Customizing Filter Chains" @ https://spring.io/guides/topicals/spring-security-architecture

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .requestMatchers()
            .antMatchers(
            "/css/**",
                "/saml/css/**",
                "/images/**",
                "/images/**",
                "/js/**",
                "/saml/js/**",
                "/gfx/**",
                "/saml/gfx/**",
                "/reactapp/**",
                "/auth/*",
                "/favicon.ico",
                "/patient/**",
                "/study/**",
                "/results/**",
                // This was needed to make sure integration test for the TokenAuthFilter in API filter chain
                // returns 401 code instead of a 302 for hitting a secured resource caused by the IDP filter chain.
                "/error"
            )
            .and()
            .authorizeRequests().antMatchers("/**").permitAll();
    }

}
