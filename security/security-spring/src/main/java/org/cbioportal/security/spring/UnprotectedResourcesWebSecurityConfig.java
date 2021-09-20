package org.cbioportal.security.spring;

import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

public class UnprotectedResourcesWebSecurityConfig {

    // Add security filter chains (note the plural) that handle paths to unprotected resources.
    // see: "Creating and Customizing Filter Chains" @ https://spring.io/guides/topicals/spring-security-architecture
    
    @Configuration
    @ConditionalOnExpression("'${authenticate}' ne 'none'")
    @Order(Ordered.HIGHEST_PRECEDENCE)
    // handles "/css/**" and "/saml/css/**" paths
    public class CssUnprotectedResourcesConfig extends WebSecurityConfigurerAdapter {
        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http.antMatcher("/{saml\\/|\\/}css/**").authorizeRequests().antMatchers("/**").permitAll();
        }
    }
    
    @Configuration
    @ConditionalOnExpression("'${authenticate}' ne 'none'")
    @Order(Ordered.HIGHEST_PRECEDENCE)
    // handles "/images/**" and "/saml/images/**" paths
    public class ImagesUnprotectedResourcesConfig extends WebSecurityConfigurerAdapter {
        @Override
        protected void configure(HttpSecurity http) throws Exception {
            // add a new security filter chain (see: "Creating and Customizing Filter Chains" @ https://spring.io/guides/topicals/spring-security-architecture
            http.antMatcher("/{saml\\/|\\/}images/**").authorizeRequests().antMatchers("/**").permitAll();
        }
    }
    
    @Configuration
    @ConditionalOnExpression("'${authenticate}' ne 'none'")
    @Order(Ordered.HIGHEST_PRECEDENCE)
    // handles "/js/**" and "/saml/js/**" paths
    public class JavascriptUnprotectedResourcesConfig extends WebSecurityConfigurerAdapter {
        @Override
        protected void configure(HttpSecurity http) throws Exception {
            // add a new security filter chain (see: "Creating and Customizing Filter Chains" @ https://spring.io/guides/topicals/spring-security-architecture
            http.antMatcher("/{saml\\/|\\/}js/**").authorizeRequests().antMatchers("/**").permitAll();
        }
    }
    
    @Configuration
    @ConditionalOnExpression("'${authenticate}' ne 'none'")
    @Order(Ordered.HIGHEST_PRECEDENCE)
    // handles "/gfx/**" and "/saml/gfx/**" paths
    public class GraphicsUnprotectedResourcesConfig extends WebSecurityConfigurerAdapter {
        @Override
        protected void configure(HttpSecurity http) throws Exception {
            // add a new security filter chain (see: "Creating and Customizing Filter Chains" @ https://spring.io/guides/topicals/spring-security-architecture
            http.antMatcher("/{saml\\/|\\/}gfx/**").authorizeRequests().antMatchers("/**").permitAll();
        }
    }
    
    @Configuration
    @ConditionalOnExpression("'${authenticate}' ne 'none'")
    @Order(Ordered.HIGHEST_PRECEDENCE)
    // handles "/gfx/**" and "/saml/gfx/**" paths
    public class ReactappUnprotectedResourcesConfig extends WebSecurityConfigurerAdapter {
        @Override
        protected void configure(HttpSecurity http) throws Exception {
            // add a new security filter chain (see: "Creating and Customizing Filter Chains" @ https://spring.io/guides/topicals/spring-security-architecture
            http.antMatcher("/reactapp/**").authorizeRequests().antMatchers("/**").permitAll();
        }
    }
    
}
