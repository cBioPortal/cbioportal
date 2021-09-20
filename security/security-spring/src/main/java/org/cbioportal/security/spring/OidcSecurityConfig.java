package org.cbioportal.security.spring;

import org.cbioportal.security.spring.authentication.PortalSavedRequestAwareAuthenticationSuccessHandler;
import org.cbioportal.security.spring.authentication.openID.PortalUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
// add new chain after api-filter chain (at position -2), but before the default fallback chain 
@Order(SecurityProperties.BASIC_AUTH_ORDER - 1)
@ConditionalOnProperty(value = "authenticate", havingValue = "openid")
public class OidcSecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private PortalUserDetailsService portalUserDetailsService;
    
    @Autowired
    private PortalSavedRequestAwareAuthenticationSuccessHandler successHandler;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            .sessionManagement().sessionFixation().none()
            .and()
            .openidLogin()
                .loginPage("/login.jsp")
                .authenticationUserDetailsService(portalUserDetailsService)
                .failureUrl("/login.jsp?login_error=true")
                .attributeExchange(".*yahoo.com.*")
                    .attribute("email",);


    }

}
