package org.cbioportal.security.spring;

import org.cbioportal.security.spring.authentication.oauth2.AccessTokenRefreshFilter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.context.SecurityContextPersistenceFilter;

@Configuration
@Order(SecurityProperties.BASIC_AUTH_ORDER - 2)
@ConditionalOnProperty(value = "authenticate", havingValue = "oauth2")
public class OAuth2ApiSecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            .addFilterAfter(accessTokenRefreshFilter(),
                SecurityContextPersistenceFilter.class)
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
    
}
