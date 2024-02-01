package org.cbioportal.security.config;

import org.cbioportal.security.basic.BasicRestfulAuthenticationSuccessHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextHolderFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.util.Objects;

import static org.springframework.security.config.Customizer.withDefaults;


@Configuration
@EnableWebSecurity
@ConditionalOnProperty(value = "authenticate", havingValue = "saml_plus_basic")
public class Saml2AndBasicConfig {
    private static final String LOGOUT_URL = "/logout";
    private static final String BASIC_LOGOUT_URL = "/j_spring_security_logout";

    @Value("${basic.username:MOCK_USER}")
    private String basicUsername;

    @Value("${basic.password:MOCK_PASSWORD}")
    private String basicPassword;
    
    @Value("${basic.authorities:}")
    private String basicAuthorities;
    

    @Bean
    public SecurityFilterChain samlFilterChain(HttpSecurity http, AuthenticationManagerBuilder authenticationManagerBuilder,
                                               LogoutSuccessHandler logoutSuccessHandler) throws Exception {
        buildAuthenticationManager(authenticationManagerBuilder, userDetailsService()); 
        http.csrf(AbstractHttpConfigurer::disable)
            .cors(withDefaults())
            .authorizeHttpRequests(auth ->
                auth.requestMatchers("/api/health", "/images/**", "/js/**", "/login").permitAll()
                    .anyRequest().authenticated())
            .exceptionHandling(eh ->
                eh.defaultAuthenticationEntryPointFor(
                    new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED), AntPathRequestMatcher.antMatcher("/api/**")
                )
            )
            .securityContext(securityContext -> securityContext.
                securityContextRepository(new HttpSessionSecurityContextRepository())
            )
            .saml2Login(withDefaults())
            .sessionManagement(sessionManagement -> sessionManagement.sessionFixation().migrateSession())
            // NOTE: I did not get the official .saml2Logout() DSL to work as
            // described at https://docs.spring.io/spring-security/reference/6.1/servlet/saml2/logout.html
            // Logout Service POST Binding URL: http://localhost:8080/logout/saml2/slo
            .logout(logout -> logout
                .logoutUrl(LOGOUT_URL)
                .clearAuthentication(true)
                .invalidateHttpSession(true)
                .logoutSuccessHandler(logoutSuccessHandler)
            )
            .logout(logout -> logout
                .logoutUrl(BASIC_LOGOUT_URL)
                .clearAuthentication(true)
                .invalidateHttpSession(true) 
            );
        
        http.apply(new BasicFilterDsl());
        return http.build();
    }

    private class BasicFilterDsl extends AbstractHttpConfigurer<BasicFilterDsl, HttpSecurity> {
        @Override
        public void configure(HttpSecurity http) {
            AuthenticationManager authenticationManager = http.getSharedObject(AuthenticationManager.class);
            UsernamePasswordAuthenticationFilter filter =
                new UsernamePasswordAuthenticationFilter();
            filter.setPostOnly(false);
            filter.setFilterProcessesUrl("/j_spring_security_check");
            filter.setUsernameParameter("j_username");
            filter.setPasswordParameter("j_password");
            filter.setAuthenticationManager(authenticationManager);
            filter.setAuthenticationSuccessHandler(new BasicRestfulAuthenticationSuccessHandler());
            http.addFilterAfter(filter, SecurityContextHolderFilter.class);
        }
    }

    public void buildAuthenticationManager(AuthenticationManagerBuilder authenticationManagerBuilder, InMemoryUserDetailsManager userDetailsManager ) {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(userDetailsManager);
        authenticationProvider.setPasswordEncoder(bCryptPasswordEncoder());
        authenticationManagerBuilder.authenticationProvider(authenticationProvider);
    }


    @Bean
    public InMemoryUserDetailsManager userDetailsService() {
        if(Objects.isNull(basicAuthorities)) {
            basicAuthorities = "NOOP";
        }
        
        UserDetails user = User
            .withUsername(this.basicUsername)
            .password(this.basicPassword)
            .roles(basicAuthorities.split(","))
            .build();
        return new InMemoryUserDetailsManager(user);
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder(10);
    }

}
