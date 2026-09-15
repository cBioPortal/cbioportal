package org.cbioportal.application.proxy;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer.FrameOptionsConfig;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security answers every request with X-Frame-Options: DENY unless told otherwise, and DENY
 * blocks framing even from the same origin — so the portal cannot embed the sidebar it is itself
 * serving. Relax it to SAMEORIGIN for the bundle's paths alone; the rest of the portal keeps DENY.
 *
 * <p>Setting the header in the controller does not work: XFrameOptionsHeaderWriter overwrites it
 * unconditionally for this mode.
 */
@Configuration
@ConditionalOnProperty(name = "chat.sidebar.url")
public class ChatSidebarSecurityConfig {

  @Bean
  @Order(SecurityProperties.BASIC_AUTH_ORDER - 2)
  public SecurityFilterChain chatSidebarFilterChain(HttpSecurity http) throws Exception {
    return http.securityMatcher("/chat-sidebar/**")
        .headers(headers -> headers.frameOptions(FrameOptionsConfig::sameOrigin))
        // Whether the bundle needs a session stays with the controller's @PreAuthorize, so this
        // chain changes framing and nothing else.
        .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
        .build();
  }
}
