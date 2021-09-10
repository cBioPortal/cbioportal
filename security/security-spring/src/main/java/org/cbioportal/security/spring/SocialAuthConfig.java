package org.cbioportal.security.spring;

import java.util.ArrayList;
import java.util.List;
import org.cbioportal.security.spring.authentication.PortalSavedRequestAwareAuthenticationSuccessHandler;
import org.cbioportal.security.spring.authentication.social.CustomUserDetailsService;
import org.cbioportal.security.spring.authentication.social.SocialConnectionSignUp;
import org.cbioportal.utils.config.annotation.ConditionalOnProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.preauth.RequestHeaderAuthenticationFilter;
import org.springframework.social.connect.mem.InMemoryUsersConnectionRepository;
import org.springframework.social.google.security.GoogleAuthenticationService;
import org.springframework.social.live.security.LiveAuthenticationService;
import org.springframework.social.security.AuthenticationNameUserIdSource;
import org.springframework.social.security.SocialAuthenticationFilter;
import org.springframework.social.security.SocialAuthenticationProvider;
import org.springframework.social.security.SocialAuthenticationServiceRegistry;
import org.springframework.social.security.provider.SocialAuthenticationService;

@Configuration
// Add new chain after api-filter chain (at position -2), but before the default fallback chain.
@Order(SecurityProperties.BASIC_AUTH_ORDER - 1)
@ConditionalOnProperty(name = "authenticate", havingValue = {"social_auth", "social_auth_google",
    "social_auth_microsoft"})
public class SocialAuthConfig extends WebSecurityConfigurerAdapter {

    // Only present when using "social_auth" or "social_auth_google".
    @Autowired(required = false)
    private GoogleAuthenticationService googleAuthenticationService;


    // Only present when using "social_auth_microsoft".
    @Autowired(required = false)
    private LiveAuthenticationService liveAuthenticationService;


    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .antMatcher("/**")
            .csrf().disable()
            .sessionManagement()
                .sessionFixation().none()
            .and()
            .addFilterBefore(socialAuthenticationFilter(), RequestHeaderAuthenticationFilter.class)
            .headers()
                .frameOptions().sameOrigin()
            .and()
            .logout()
                .logoutUrl("/j_spring_security_logout")
                .logoutSuccessUrl("/")
                .deleteCookies("JSESSIONID");
    }

    // Update the Spring Boot AuthenticationManager to contain a socialAuthenticationProvider
    // (see: "Customizing Authentication Managers" @ https://spring.io/guides/topicals/spring-security-architecture
    @Autowired
    public void initialize(AuthenticationManagerBuilder builder) {
        if (socialAuthenticationProvider() != null) {
            builder.authenticationProvider(socialAuthenticationProvider());
        }
    }

    @Bean
    public PortalSavedRequestAwareAuthenticationSuccessHandler authenticationSuccessHandler() {
        PortalSavedRequestAwareAuthenticationSuccessHandler
            handler =
            new PortalSavedRequestAwareAuthenticationSuccessHandler();
        handler.setDefaultTargetUrl("/restore?key=login-redirect");
        handler.setAlwaysUseDefaultTargetUrl(true);
        handler.setTargetUrlParameter("spring-security-redirect");
        return handler;
    }

    @Bean
    public SocialAuthenticationFilter socialAuthenticationFilter() throws Exception {
        SocialAuthenticationFilter filter = new SocialAuthenticationFilter(
            authenticationManager(),
            authenticationNameUserIdSource(),
            userConnectionRepository(),
            socialAuthenticationServiceRegistry()
        );
        filter.setSignupUrl("/login.html");
        filter.setDefaultFailureUrl("/login.html?login_error=true");
        filter.setPostLoginUrl("/index.html");
        filter.setAuthenticationSuccessHandler(authenticationSuccessHandler());
        filter.setAllowSessionCreation(true);
        return filter;
    }

    @Bean
    public LoginUrlAuthenticationEntryPoint loginUrlAuthenticationEntryPoint() {
        return new LoginUrlAuthenticationEntryPoint("/index.html");
    }

    @Bean
    public SocialAuthenticationProvider socialAuthenticationProvider() {
        return new SocialAuthenticationProvider(userConnectionRepository(),
            socialUserDetailsService());
    }

    @Bean
    public CustomUserDetailsService socialUserDetailsService() {
        return new CustomUserDetailsService();
    }

    @Bean
    public AuthenticationNameUserIdSource authenticationNameUserIdSource() {
        return new AuthenticationNameUserIdSource();
    }

    @Bean
    public InMemoryUsersConnectionRepository userConnectionRepository() {
        InMemoryUsersConnectionRepository connectionRepository =
            new InMemoryUsersConnectionRepository(socialAuthenticationServiceRegistry());
        connectionRepository.setConnectionSignUp(connectionSignUp());
        return connectionRepository;
    }

    @Bean
    public SocialConnectionSignUp connectionSignUp() {
        return new SocialConnectionSignUp();
    }

    @Bean
    public SocialAuthenticationServiceRegistry socialAuthenticationServiceRegistry() {
        SocialAuthenticationServiceRegistry serviceRegistry =
            new SocialAuthenticationServiceRegistry();
        List<SocialAuthenticationService<?>> connectionFactories = new ArrayList<>();
        if (googleAuthenticationService != null) {
            connectionFactories.add(googleAuthenticationService);
        }
        if (liveAuthenticationService != null) {
            connectionFactories.add(liveAuthenticationService);
        }
        serviceRegistry.setAuthenticationServices(connectionFactories);
        return serviceRegistry;
    }

}
