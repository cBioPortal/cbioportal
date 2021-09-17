package org.cbioportal.security.spring;

import org.cbioportal.security.spring.authentication.token.TokenAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@ConditionalOnExpression("${authenticate} ne none")
public class AuthenticatedWebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private TokenAuthenticationFilter tokenAuthenticationFilter;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            .authorizeRequests()
            .antMatchers(
                // static resources not processed by spring security filters
                "/css/**",
                "/saml/css/**",
                "/images/**",
                "/saml/images/**",
                "/js/**",
                "/saml/js/**",
                "/gfx/**",
                "/saml/gfx/**",
                "/reactapp/**",
                "/api/swagger-resources/**",
                "/api/swagger-ui.html",
                "/api/health",
                "/api/cache"
            ).permitAll()
            .anyRequest().authenticated()
            .antMatchers("/api/**", "/webservice.do")
                .authenticated()
                .and()
                .addFilterBefore(tokenAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
    }

}
