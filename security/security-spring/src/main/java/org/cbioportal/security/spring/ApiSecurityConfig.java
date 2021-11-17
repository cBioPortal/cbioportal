package org.cbioportal.security.spring;

import org.cbioportal.security.spring.authentication.RestAuthenticationEntryPoint;
import org.cbioportal.security.spring.authentication.oauth2.OAuth2AccessTokenRefreshFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.context.SecurityContextPersistenceFilter;

@Configuration
@Order(SecurityProperties.BASIC_AUTH_ORDER - 2)
@ConditionalOnExpression("'${authenticate}' ne 'false' && '${authenticate}' ne 'noauthsessionservice' && '${dat.method:none}' eq 'none'")
public class ApiSecurityConfig extends WebSecurityConfigurerAdapter {

    // Add security filter chains that handle calls to the API endpoints.
    // Different chains are added for the '/api' and legacy '/webservice.do' paths.
    // Both are able to handle API tokens provided in the request.
    // see: "Creating and Customizing Filter Chains" @ https://spring.io/guides/topicals/spring-security-architecture
    
    @Value("${authenticate}")
    private String authenticate;
    
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            .sessionManagement()
                .sessionFixation().none()
            .and()
                // add ExceptionTranslationFilter to the chain
                .exceptionHandling()                    
                .authenticationEntryPoint(restAuthenticationEntryPoint())
            .and()
            .antMatcher("/api/**")
                .authorizeRequests()
                    .antMatchers("/api/swagger-resources/**",
                         "/api/swagger-ui.html",
                         "/api/health",
                         "/api/cache/**").permitAll()
                    .anyRequest()
                        .authenticated();
        if (authenticate.equals("oauth2")) {
            http
                .addFilterAfter(accessTokenRefreshFilter(), SecurityContextPersistenceFilter.class);
        }
    }
    
    @Bean
    public OAuth2AccessTokenRefreshFilter accessTokenRefreshFilter() {
        return new OAuth2AccessTokenRefreshFilter();
    }
    
    @Bean
    public RestAuthenticationEntryPoint restAuthenticationEntryPoint() {
        return new RestAuthenticationEntryPoint();
    }
 
}
