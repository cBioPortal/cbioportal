package org.cbioportal.security.config;

import org.cbioportal.security.token.RestAuthenticationEntryPoint;
import org.cbioportal.security.token.TokenAuthenticationFilter;
import org.cbioportal.security.token.TokenAuthenticationSuccessHandler;
import org.cbioportal.service.DataAccessTokenService;
import org.cbioportal.utils.config.annotation.ConditionalOnProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.Nullable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.SecurityContextPersistenceFilter;

@Configuration
@ConditionalOnProperty(name = "authenticate", havingValue = {"false", "noauthsessionservice", "optional_oauth2"}, isNot = true)
public class ApiSecurityConfig {

    // Add security filter chains that handle calls to the API endpoints.
    // Different chains are added for the '/api' and legacy '/webservice.do' paths.
    // Both are able to handle API tokens provided in the request.
    // see: "Creating and Customizing Filter Chains" @ https://spring.io/guides/topicals/spring-security-architecture

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, @Nullable DataAccessTokenService tokenService) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
            // This filter chain only grabs requests to the '/api' path.
            .securityMatcher("/api/**", "/webservice.do")
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers(
                    "/api/swagger-resources/**",
                    "/api/swagger-ui.html",
                    "/api/health",
                    "/api/cache/**").permitAll()
                .anyRequest().authenticated()
            )
            .sessionManagement(sessionManagement -> sessionManagement.sessionFixation().migrateSession())
            .exceptionHandling(exceptionHandling -> exceptionHandling
                .authenticationEntryPoint(restAuthenticationEntryPoint())
            );
        // When dat.method is not 'none' and a tokenService bean is present,
        // the apiTokenAuthenticationFilter is added to the filter chain.
        if (tokenService != null) {
            http.apply(ApiTokenFilterDsl.tokenFilterDsl(tokenService));
        }
        return http.build();
    }
    
    @Autowired
    public AuthenticationManagerBuilder buildAuthenticationManager(AuthenticationManagerBuilder authenticationManagerBuilder, @Nullable AuthenticationProvider tokenAuthenticationProvider) {
        if (tokenAuthenticationProvider != null) {
            authenticationManagerBuilder.authenticationProvider(tokenAuthenticationProvider);
        }
        return authenticationManagerBuilder;
    }
    
    @Bean
    public RestAuthenticationEntryPoint restAuthenticationEntryPoint() {
        return new RestAuthenticationEntryPoint();
    }
    
}


class ApiTokenFilterDsl extends AbstractHttpConfigurer<ApiTokenFilterDsl, HttpSecurity> {

    private final DataAccessTokenService tokenService;

    public ApiTokenFilterDsl(DataAccessTokenService tokenService) {
        this.tokenService = tokenService;
    }
    
    @Override
    public void configure(HttpSecurity http) {
        AuthenticationManager authenticationManager = http.getSharedObject(AuthenticationManager.class);
        TokenAuthenticationSuccessHandler tokenAuthenticationSuccessHandler = new TokenAuthenticationSuccessHandler();
        TokenAuthenticationFilter filter = new TokenAuthenticationFilter("/**", authenticationManager, tokenService);
        filter.setAuthenticationSuccessHandler(tokenAuthenticationSuccessHandler);
        http.addFilterAfter(filter, SecurityContextPersistenceFilter.class);
    }
    
    public static ApiTokenFilterDsl tokenFilterDsl(DataAccessTokenService tokenService) {
        return new ApiTokenFilterDsl(tokenService);
    }
    
}
