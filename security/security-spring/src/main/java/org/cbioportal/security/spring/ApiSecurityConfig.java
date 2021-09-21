package org.cbioportal.security.spring;

import org.cbioportal.security.spring.authentication.RestAuthenticationEntryPoint;
import org.cbioportal.security.spring.authentication.token.TokenAuthenticationFilter;
import org.cbioportal.security.spring.authentication.token.TokenAuthenticationSuccessHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@Order(SecurityProperties.BASIC_AUTH_ORDER - 2)
@ConditionalOnExpression("'${authenticate}' ne 'none'")
public class ApiSecurityConfig extends WebSecurityConfigurerAdapter {

    // Add security filter chains that handle calls to the API endpoints.
    // Different chains are added for the '/api' and legacy '/webservice.do' paths.
    // Both are able to handle API tokens provided in the request.
    // see: "Creating and Customizing Filter Chains" @ https://spring.io/guides/topicals/spring-security-architecture

    @Autowired
    private AuthenticationProvider tokenAuthenticationProvider;

    @Autowired
    private TokenAuthenticationSuccessHandler tokenAuthenticationSuccessHandler;

    // Replace the default Spring Boot AuthenticationManager with a new one
    // containing the tokenAuthenticationProvider (see: "Customizing Authentication Managers"
    // @ https://spring.io/guides/topicals/spring-security-architecture
    @Autowired
    public void initialize(AuthenticationManagerBuilder builder) {
        builder.authenticationProvider(tokenAuthenticationProvider);
    }

    @Bean
    public TokenAuthenticationFilter tokenAuthenticationFilter() throws Exception {
        TokenAuthenticationFilter tokenAuthenticationFilter =
            new TokenAuthenticationFilter("/**", authenticationManager());
        tokenAuthenticationFilter.setAuthenticationSuccessHandler(tokenAuthenticationSuccessHandler);
        return tokenAuthenticationFilter;
    }

    @Autowired
    private RestAuthenticationEntryPoint restAuthenticationEntryPoint;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            .addFilterBefore(tokenAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
            .sessionManagement().sessionFixation().none()
            .and()
            .exceptionHandling().authenticationEntryPoint(restAuthenticationEntryPoint)
            .and()
            .antMatcher("/api/**")
                .authorizeRequests()
                    .antMatchers("/api/swagger-resources/**",
                         "/api/swagger-ui.html",
                         "/api/health",
                         "/api/cache").permitAll()
                    .antMatchers("/api/**").authenticated()
            .and()
            // TODO should this not be "/webservice.do*"?
            // TODO this overwrites the previous .antMatcher call - refactor in to new WebSecurityConfigurerAdapter block
            .antMatcher("/webservice.do")
                .authorizeRequests()
                    .antMatchers("/**").authenticated();
    }
 
}
