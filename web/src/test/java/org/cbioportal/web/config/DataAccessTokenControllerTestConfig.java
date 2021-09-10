package org.cbioportal.web.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@TestConfiguration
public class DataAccessTokenControllerTestConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.antMatcher("/**")
            .authorizeRequests().anyRequest().authenticated()
            .and()
            .addFilter(usernamePasswordAuthenticationFilter())
            .httpBasic()
            .authenticationEntryPoint(restAuthenticationEntryPoint());
    }

    // OK
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        PasswordEncoder encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
        auth.inMemoryAuthentication()
            .passwordEncoder(encoder)
            .withUser("MOCK_USER")
            .password(encoder.encode("{noop}MOCK_PASSWORD"))
            .authorities("PLACEHOLDER_ROLE");
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
        filter.setFilterProcessesUrl("j_spring_security_check");
        filter.setUsernameParameter("j_username");
        filter.setPasswordParameter("j_password");
        filter.setAuthenticationManager(authenticationManagerBean());
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

}
