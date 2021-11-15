package org.cbioportal.security.spring;

import java.io.IOException;
import java.time.Instant;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.web.filter.GenericFilterBean;

@Configuration
@Order(SecurityProperties.BASIC_AUTH_ORDER - 2)
@ConditionalOnProperty(value = "authenticate", havingValue = "oauth2")
public class OAuth2ApiSecurityConfig extends WebSecurityConfigurerAdapter {

//    @Autowired
//    private OAuth2AuthorizedClientService authorizedClientService;
    
    @Autowired
    private AuthenticationProvider authenticationProvider;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
//            .addFilterBefore(refreshTokenFilter(authorizedClientService),  AnonymousAuthenticationFilter.class)
            .antMatcher("/api/**")
                .authorizeRequests()
                .anyRequest()
                    .authenticated();
    }

//    @Bean
//    public GenericFilterBean refreshTokenFilter(OAuth2AuthorizedClientService clientService) {
//        return new GenericFilterBean() {
//            @Override
//            public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
//                                 FilterChain filterChain) throws ServletException, IOException {
//                Authentication authentication =
//                    SecurityContextHolder.getContext().getAuthentication();
//                if (authentication != null && authentication instanceof OAuth2AuthenticationToken) {
//                    OAuth2AuthenticationToken token = (OAuth2AuthenticationToken) authentication;
//                    OAuth2AuthorizedClient client =
//                        clientService.loadAuthorizedClient(
//                            token.getAuthorizedClientRegistrationId(),
//                            token.getName());
//                    OAuth2AccessToken accessToken = client.getAccessToken();
//                    if (accessToken.getExpiresAt().isBefore(Instant.now())) {
//                        // TODO is it needed to refresh the token, and recreate the Authentication object 
//                        SecurityContextHolder.getContext().setAuthentication(null);
//                    }
//                }
//                filterChain.doFilter(servletRequest, servletResponse);
//            }
//        };
//    }

}
