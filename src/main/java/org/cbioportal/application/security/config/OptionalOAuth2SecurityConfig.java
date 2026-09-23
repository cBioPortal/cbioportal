package org.cbioportal.application.security.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
// add new chain after api-filter chain (at position -2), but before the default fallback chain.
// SecurityProperties.BASIC_AUTH_ORDER (Ordered.LOWEST_PRECEDENCE - 5) was removed in Spring
// Boot 4; inlined here to keep this filter chain at the same relative position.
@Order(Ordered.LOWEST_PRECEDENCE - 5 - 1)
@ConditionalOnProperty(value = "authenticate", havingValue = "optional_oauth2")
public class OptionalOAuth2SecurityConfig {

  @Bean
  public SecurityFilterChain optionalOAuth2filterChain(HttpSecurity http) throws Exception {
    return http.oauth2Login(oauth -> oauth.loginPage("/login"))
        .authorizeHttpRequests(
            auth -> auth.requestMatchers("/").permitAll().anyRequest().permitAll())
        .cors(Customizer.withDefaults())
        .csrf(AbstractHttpConfigurer::disable)
        .logout(logout -> logout.logoutSuccessUrl("/"))
        .build();
  }
}
