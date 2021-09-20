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
public class UnprotectedResourcesWebSecurityConfig extends WebSecurityConfigurerAdapter {

    // Add security filter chains (note the plural) that handle paths to unprotected resources.
    // see: "Creating and Customizing Filter Chains" @ https://spring.io/guides/topicals/spring-security-architecture
    
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            // handles "/css/**" and "/saml/css/**" paths
            .antMatcher("/{saml\\/|\\/}css/**").authorizeRequests().antMatchers("/**").permitAll()
            .and()
            // handles "/images/**" and "/saml/images/**" paths
            .antMatcher("/{saml\\/|\\/}images/**").authorizeRequests().antMatchers("/**").permitAll()
            .and()
            // handles "/js/**" and "/saml/js/**" paths
            .antMatcher("/{saml\\/|\\/}js/**").authorizeRequests().antMatchers("/**").permitAll()
            .and()
            // handles "/gfx/**" and "/saml/gfx/**" paths
            .antMatcher("/{saml\\/|\\/}gfx/**").authorizeRequests().antMatchers("/**").permitAll()
            .and()
            .antMatcher("/reactapp/**").authorizeRequests().antMatchers("/**").permitAll();
    }

}
