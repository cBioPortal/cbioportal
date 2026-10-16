package org.cbioportal.application.security.config;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class CorsConfig {
  private static final String SAML_RESPONSE_PATH_PREFIX = "/login/saml2/sso/";

  @Value("${security.cors.allowed-origins:}")
  private String allowedOrigins;

  @Value("${security.cors.allow-credentials:false}")
  private boolean allowCredentials;

  @Bean
  CorsConfigurationSource corsConfigurationSource() {
    List<String> parsedAllowedOrigins =
        Arrays.stream(allowedOrigins.split(","))
            .map(String::trim)
            .filter(origin -> !origin.isEmpty())
            .toList();
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    // No origins configured: register nothing, so the CORS filter lets every request through
    // unchanged instead of rejecting each cross-origin one.
    if (parsedAllowedOrigins.isEmpty()) {
      return source;
    }

    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(parsedAllowedOrigins);
    // Credentials (the login cookie) are opt-in, for a frontend hosted on another origin. Spring
    // rejects them with the "*" wildcard, so the wildcard keeps them off.
    configuration.setAllowCredentials(allowCredentials && !parsedAllowedOrigins.contains("*"));
    configuration.setAllowedMethods(List.of("GET", "POST", "HEAD", "OPTIONS"));
    configuration.setAllowedHeaders(
        List.of(
            "user-agent",
            "Origin",
            "Accept",
            "X-Requested-With",
            "Content-Type",
            "Authorization",
            "Access-Control-Request-Method",
            "Access-Control-Request-Headers",
            "Content-Encoding",
            "X-Proxy-User-Agreement",
            "x-current-url"));
    configuration.setExposedHeaders(List.of("total-count", "sample-count", "elapsed-time"));
    source.registerCorsConfiguration("/**", configuration);
    return request -> isSamlResponseRequest(request) ? null : source.getCorsConfiguration(request);
  }

  private static boolean isSamlResponseRequest(HttpServletRequest request) {
    String requestPath = request.getRequestURI();
    String contextPath = request.getContextPath();
    if (!contextPath.isEmpty() && requestPath.startsWith(contextPath)) {
      requestPath = requestPath.substring(contextPath.length());
    }
    return requestPath.startsWith(SAML_RESPONSE_PATH_PREFIX);
  }
}
