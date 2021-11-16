package org.cbioportal.security.spring;

import org.cbioportal.security.spring.authentication.oauth2.AccessTokenRefreshFilter;
import org.cbioportal.security.spring.authentication.token.TokenAuthenticationFilter;
import org.cbioportal.security.spring.authentication.token.TokenAuthenticationSuccessHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.SecurityContextPersistenceFilter;

@Configuration
@Order(SecurityProperties.BASIC_AUTH_ORDER - 2)
@ConditionalOnProperty(value = "authenticate", havingValue = "oauth2")
public class OAuth2ApiSecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private TokenAuthenticationSuccessHandler tokenAuthenticationSuccessHandler;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            // needed for refresh of access token and user permissions (user agent is web browser)
            .addFilterAfter(accessTokenRefreshFilter(), SecurityContextPersistenceFilter.class)
            // needed for exchange of offline token for access token and eval of user 
            // persmissions (for programmatic access)
            .addFilterAfter(tokenAuthenticationFilter(), SecurityContextPersistenceFilter.class)
            .sessionManagement()
                .sessionFixation().none()
            .and()
            .antMatcher("/api/**")
                .authorizeRequests()
                    .antMatchers("/api/swagger-resources/**",
                        "/api/swagger-ui.html",
                        "/api/health",
                        "/api/cache/**").permitAll()
                .anyRequest()
                    .authenticated();
    }

    @Bean
    public AccessTokenRefreshFilter accessTokenRefreshFilter() {
        return new AccessTokenRefreshFilter();
    }

    @Bean
    public TokenAuthenticationFilter tokenAuthenticationFilter() throws Exception {
        TokenAuthenticationFilter tokenAuthenticationFilter =
            new TokenAuthenticationFilter("/**", authenticationManager());
        tokenAuthenticationFilter.setAuthenticationSuccessHandler(tokenAuthenticationSuccessHandler);
        return tokenAuthenticationFilter;
    }
    
}
