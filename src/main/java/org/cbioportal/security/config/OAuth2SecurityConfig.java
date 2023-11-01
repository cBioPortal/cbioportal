package org.cbioportal.security.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;



@Configuration
@EnableWebSecurity
// add new chain after api-filter chain (at position -2), but before the default fallback chain 
@Order(SecurityProperties.BASIC_AUTH_ORDER - 1)
public class OAuth2SecurityConfig {

    @Bean
    @ConditionalOnProperty(value = "authenticate", havingValue = "oauth2")
    public SecurityFilterChain oAuth2filterChain(HttpSecurity http) throws Exception {
        return http.authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
            .oauth2Login(Customizer.withDefaults())
            .csrf(AbstractHttpConfigurer::disable)
            .build();
    }

    @Bean
    @ConditionalOnProperty(value = "authenticate", havingValue = "optional_oauth2")
    public SecurityFilterChain optionalOAuth2filterChain(HttpSecurity http) throws Exception {
        return http
            .oauth2Login(Customizer.withDefaults())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/").permitAll()
                .anyRequest().permitAll())
            .csrf(AbstractHttpConfigurer::disable)
            .build();
    } 
}
