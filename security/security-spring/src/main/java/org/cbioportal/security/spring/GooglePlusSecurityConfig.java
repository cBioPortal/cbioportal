package org.cbioportal.security.spring;

import java.util.Arrays;
import org.cbioportal.security.spring.authentication.PortalSavedRequestAwareAuthenticationSuccessHandler;
import org.cbioportal.security.spring.authentication.googleplus.GoogleplusConnectionFactory;
import org.cbioportal.security.spring.authentication.googleplus.GoogleplusUserDetailsService;
import org.cbioportal.security.spring.authentication.PortalUserDetailsService;
import org.cbioportal.security.spring.authentication.social.SocialConnectionSignUp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.social.connect.UsersConnectionRepository;
import org.springframework.social.connect.mem.InMemoryUsersConnectionRepository;
import org.springframework.social.google.security.GoogleAuthenticationService;
import org.springframework.social.security.AuthenticationNameUserIdSource;
import org.springframework.social.security.SocialAuthenticationFilter;
import org.springframework.social.security.SocialAuthenticationProvider;
import org.springframework.social.security.SocialAuthenticationServiceRegistry;

// TODO decide on removal of this security option
@Configuration
// add new chain after api-filter chain (at position -2), but before the default fallback chain 
@Order(SecurityProperties.BASIC_AUTH_ORDER - 1)
@ConditionalOnProperty(value = "authenticate", havingValue = "googleplus")
public class GooglePlusSecurityConfig extends WebSecurityConfigurerAdapter {

    @Value("${googleplus.consumer.key}")
    private String googleplusConsumerKey;
    
    @Value("${googleplus.consumer.secret}")
    private String googleplusConsumerSecret;
    
    @Autowired
    private PortalUserDetailsService portalUserDetailsService;
    
    @Autowired
    private PortalSavedRequestAwareAuthenticationSuccessHandler successHandler;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .antMatcher("/**")
            .csrf().disable()
            // this was before preauthentication filter in original xml config; I do not see a good reason for this
            .addFilterBefore(socialAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
            .sessionManagement().sessionFixation().none()
            .and()
                .authorizeRequests()
                    .antMatchers("/**")
                        .authenticated()
            .and()
            .logout()
                .logoutUrl("/j_spring_security_logout")
                .logoutSuccessUrl("/login.html?logout_success=true")
                .deleteCookies("JSESSIONID");

    }

    // Add the googleplusAuthenticationProvider to the AuthenticationManager that 
    // contains the tokenAuthenticationProvider created in ApiWebSecurityConfig
    // (see: "Customizing Authentication Managers" @ https://spring.io/guides/topicals/spring-security-architecture
    @Override
    public void configure(AuthenticationManagerBuilder builder) {
        builder.authenticationProvider(socialAuthenticationProvider());
    }

    @Bean
    public SocialAuthenticationFilter socialAuthenticationFilter() throws Exception {
        SocialAuthenticationFilter socialAuthenticationFilter = new SocialAuthenticationFilter(
            authenticationManager(), authenticationNameUserIdSource(), usersConnectionRepository(),
            connectionFactoryLocator());
        socialAuthenticationFilter.setSignupUrl("/login.html");
        socialAuthenticationFilter.setDefaultFailureUrl("/login.html?login_error=true");
        socialAuthenticationFilter.setPostLoginUrl("/index.do");
        socialAuthenticationFilter.setAuthenticationSuccessHandler(successHandler);
        socialAuthenticationFilter.setAllowSessionCreation(true);
        return socialAuthenticationFilter;
    }
    
    @Bean
    public SocialAuthenticationProvider socialAuthenticationProvider() {
        return new SocialAuthenticationProvider(usersConnectionRepository(), googleplusUserDetailsService());
    }
    
    @Bean
    public AuthenticationNameUserIdSource authenticationNameUserIdSource() {
        return new AuthenticationNameUserIdSource();
    }
    
    @Bean
    public UsersConnectionRepository usersConnectionRepository() {
        InMemoryUsersConnectionRepository usersConnectionRepository =
            new InMemoryUsersConnectionRepository(connectionFactoryLocator());
        usersConnectionRepository.setConnectionSignUp(socialConnectionSignUp());
        return usersConnectionRepository;
    }
    
    @Bean
    public SocialConnectionSignUp socialConnectionSignUp() {
        return new SocialConnectionSignUp();
    }
    
    @Bean
    public SocialAuthenticationServiceRegistry connectionFactoryLocator() {
        SocialAuthenticationServiceRegistry registry =
            new SocialAuthenticationServiceRegistry();
        registry.setAuthenticationServices(Arrays.asList(
            googleAuthenticationService()
        ));
        return registry;
    }
    
    @Bean
    public GoogleAuthenticationService googleAuthenticationService() {
        GoogleAuthenticationService authenticationService = new GoogleAuthenticationService(
            googleplusConsumerKey, googleplusConsumerSecret
        );
        authenticationService.setConnectionFactory(connectionFactory());
        authenticationService.setDefaultScope("email");
        return authenticationService;
    }
    
    @Bean
    public GoogleplusConnectionFactory connectionFactory() {
        return new GoogleplusConnectionFactory(googleplusConsumerKey, googleplusConsumerSecret);
    }
    
    @Bean
    public GoogleplusUserDetailsService googleplusUserDetailsService() {
        return new GoogleplusUserDetailsService(portalUserDetailsService);
    } 

}
