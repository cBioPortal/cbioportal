package org.cbioportal.security.spring;

import org.cbioportal.security.spring.authentication.oauth2.OAuth2AccessTokenRefreshFilter;
import org.cbioportal.security.spring.authentication.oauth2.OidcClientInitiatedLogoutSuccessHandler;
import org.cbioportal.security.spring.authentication.oauth2.UserInfoAuthoritiesMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;


@Configuration
// add new chain after api-filter chain (at position -2), but before the default fallback chain 
@Order(SecurityProperties.BASIC_AUTH_ORDER - 1)
@ConditionalOnProperty(value = "authenticate", havingValue = "oauth2")
public class OAuth2SecurityConfig extends WebSecurityConfigurerAdapter {

    @Value("${spring.security.oauth2.client.jwt-roles-path:resource_access::cbioportal::roles}")
    private String jwtRolesPath;
    
    @Value("${spring.security.oauth2.client.provider.cbio-idp.logout-uri:not-defined}")
    private String idpOidcLogoutUrl;

    // TODO create single logout URL for all implementations
    @Value("${spring.security.oauth2.logout-url:/j_spring_security_logout}")
    private String logoutUrl;

    @Autowired
    private ClientRegistrationRepository clientRegistrationRepository;

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
                    .userAuthoritiesMapper(new UserInfoAuthoritiesMapper(jwtRolesPath))
                .and()
            .and()
                .logout()
                    .logoutUrl(logoutUrl)
                    .logoutSuccessHandler(
                        logoutSuccessHandler()  // when successful this handler will trigger SSO logout
                    )
                    .deleteCookies("JSESSIONID");
    }

    @Bean
    public LogoutSuccessHandler logoutSuccessHandler() {
        OidcClientInitiatedLogoutSuccessHandler oidcLogoutSuccessHandler =
            new OidcClientInitiatedLogoutSuccessHandler(clientRegistrationRepository);
        // TODO this can be removed when Spring Security issue has been resolved
        // See: https://github.com/spring-projects/spring-security/issues/9669
        // See: https://github.com/spring-projects/spring-security/issues/10059
        if (!"not-defined".equals(idpOidcLogoutUrl)) {
            oidcLogoutSuccessHandler.setEndSessionEndpoint(idpOidcLogoutUrl);
        }
        oidcLogoutSuccessHandler.setPostLogoutRedirectUri("{baseUrl}");
        return oidcLogoutSuccessHandler;
    }

    @Bean
    public OAuth2AccessTokenRefreshFilter accessTokenRefreshFilter() {
        return new OAuth2AccessTokenRefreshFilter();
    }

}
