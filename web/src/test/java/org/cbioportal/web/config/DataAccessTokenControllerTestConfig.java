package org.cbioportal.web.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.ArrayList;
import java.util.List;

@TestConfiguration
@EnableWebSecurity
public class DataAccessTokenControllerTestConfig extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.antMatcher("/**")
            .authorizeRequests().anyRequest().authenticated()
            .and()
            .httpBasic()
            .authenticationEntryPoint(restAuthenticationEntryPoint());
    }

    // OK
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        PasswordEncoder encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
        // Define the authorities for the mock user
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("PLACEHOLDER_ROLE"));
        UserDetails mockUser = User.withUsername("MOCK_USER")
                .password("{noop}MOCK_PASSWORD")
                .roles("PLACEHOLDER_ROLE")
                .authorities(authorities)
                .build();
        
            auth.inMemoryAuthentication()
            .passwordEncoder(encoder).withUser(mockUser);
    }

    @Bean
    public RestAuthenticationEntryPoint restAuthenticationEntryPoint() {
        return new RestAuthenticationEntryPoint();
    }

    @Bean
    public UsernamePasswordAuthenticationFilter usernamePasswordAuthenticationFilter()
        throws Exception {
        UsernamePasswordAuthenticationFilter filter =
            new UsernamePasswordAuthenticationFilter();
        filter.setPostOnly(false);
        filter.setFilterProcessesUrl("/j_spring_security_check");
        filter.setUsernameParameter("j_username");
        filter.setPasswordParameter("j_password");
        filter.setAuthenticationManager(authenticationManagerBean());
        filter.setAuthenticationSuccessHandler(tokenAuthenticationSuccessHandler());
        return filter;
    }

    @Bean
    public TokenAuthenticationSuccessHandler tokenAuthenticationSuccessHandler() {
        return new TokenAuthenticationSuccessHandler();
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return super.userDetailsService();
    }

}
