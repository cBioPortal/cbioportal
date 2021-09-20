package org.cbioportal.security.spring;

import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
@ConditionalOnExpression("'${authenticate}' ne 'none'")
@Order(Ordered.HIGHEST_PRECEDENCE)
public class UnprotectedResourcesSecurityConfig extends WebSecurityConfigurerAdapter {

    // Add security filter chains (note the plural) that handle paths to unprotected resources.
    // see: "Creating and Customizing Filter Chains" @ https://spring.io/guides/topicals/spring-security-architecture
    
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .antMatcher("/css/**").authorizeRequests().antMatchers("/**").permitAll()
            .and()
            .antMatcher("/saml/css/**").authorizeRequests().antMatchers("/**").permitAll()
            .and()
            .antMatcher("/images/**").authorizeRequests().antMatchers("/**").permitAll()
            .and()
            .antMatcher("/saml/images/**").authorizeRequests().antMatchers("/**").permitAll()
            .and()
            .antMatcher("/js/**").authorizeRequests().antMatchers("/**").permitAll()
            .and()
            .antMatcher("/saml/js/**").authorizeRequests().antMatchers("/**").permitAll()
            .and()
            .antMatcher("/gfx/**").authorizeRequests().antMatchers("/**").permitAll()
            .and()
            .antMatcher("/saml/gfx/**").authorizeRequests().antMatchers("/**").permitAll()
            .and()
            .antMatcher("/reactapp/**").authorizeRequests().antMatchers("/**").permitAll();
    }

}
