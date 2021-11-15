package org.cbioportal.security.spring;

import org.cbioportal.security.spring.authentication.oauth2.CBioAuthoritiesMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;


@Configuration
// add new chain after api-filter chain (at position -2), but before the default fallback chain 
@Order(SecurityProperties.BASIC_AUTH_ORDER - 1)
@ConditionalOnProperty(value = "authenticate", havingValue = "oauth2")
public class OAuth2SecurityConfig extends WebSecurityConfigurerAdapter {

    @Value("${spring.security.oauth2.client.jwt-roles-path:resource_access::cbioportal::roles}")
    private String jwtRolesPath;

    @Override
    // Autoconfigure by Spring Boot
    // See: https://docs.spring.io/spring-security/site/docs/5.5.x/reference/html5/#oauth2login-override-boot-autoconfig
    protected void configure(HttpSecurity http) throws Exception {
        http
            .antMatcher("/**")
            .csrf().disable()
            .authorizeRequests()
                .anyRequest()
                    .authenticated()
            .and()
            .oauth2Login()
                .userInfoEndpoint()
                    .userAuthoritiesMapper(new CBioAuthoritiesMapper(jwtRolesPath))
                .and()
            .and()
                .logout()
                    .logoutSuccessUrl("/");
    }

}
