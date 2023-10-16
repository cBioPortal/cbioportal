package org.cbioportal.web.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@TestConfiguration
public class DataAccessTokenControllerTestConfig {
    
    @Bean
    protected SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests((authz) -> authz
                .anyRequest().authenticated())  
            .apply(new TestFilterDsl())
            .and()
            .httpBasic()
            .authenticationEntryPoint(restAuthenticationEntryPoint());
        return http.build();
    }

    @Bean
    public InMemoryUserDetailsManager userDetailsService() {
        UserDetails user = User
            .withUsername("MOCK_USER")
            .password(noopPasswordEncoder().encode("MOCK_PASSWORD"))
            .roles("PLACEHOLDER_ROLE")
            .build();
        return new InMemoryUserDetailsManager(user);
    }

    @Bean
    public static NoOpPasswordEncoder noopPasswordEncoder() {
        return (NoOpPasswordEncoder) NoOpPasswordEncoder.getInstance();
    }

    private class TestFilterDsl extends AbstractHttpConfigurer<TestFilterDsl, HttpSecurity> {
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
            filter.setAuthenticationSuccessHandler(tokenAuthenticationSuccessHandler());
            http.addFilter(filter);
        }
    }

    @Bean
    public RestAuthenticationEntryPoint restAuthenticationEntryPoint() {
        return new RestAuthenticationEntryPoint();
    }

    @Bean
    public TokenAuthenticationSuccessHandler tokenAuthenticationSuccessHandler() {
        return new TokenAuthenticationSuccessHandler();
    }

}
