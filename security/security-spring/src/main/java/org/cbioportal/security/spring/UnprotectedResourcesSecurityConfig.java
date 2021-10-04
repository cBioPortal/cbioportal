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
            .antMatcher("/css/**").authorizeRequests().antMatchers("/**").permitAll()
            .and()
            // TODO this overwrites the previous .antMatcher call - refactor in to new WebSecurityConfigurerAdapter block
            // see: https://docs.spring.io/spring-security/site/docs/4.2.x/apidocs/org/springframework/security/config/annotation/web/builders/HttpSecurity.html#antMatcher-java.lang.String-
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
            .antMatcher("/reactapp/**").authorizeRequests().antMatchers("/**").permitAll()
            
            // added from the google, saml and oidc filter chains of the original xml config
            // TODO I think these are better placed here; better confirm this
            .and()
            .antMatcher("/auth/*").authorizeRequests().antMatchers("/**").permitAll()
            .and()
            .antMatcher("/favicon.ico").authorizeRequests().antMatchers("/**").permitAll()
            .and()
            .antMatcher("/case.do*").authorizeRequests().antMatchers("/**").permitAll()
            .and()
            .antMatcher("/patient/**").authorizeRequests().antMatchers("/**").permitAll()
            .and()
            .antMatcher("/study/**").authorizeRequests().antMatchers("/**").permitAll()
            .and()
            .antMatcher("/results/**").authorizeRequests().antMatchers("/**").permitAll()
            .and()
            .antMatcher("/network.do*").authorizeRequests().antMatchers("/**").permitAll();
    }

}
