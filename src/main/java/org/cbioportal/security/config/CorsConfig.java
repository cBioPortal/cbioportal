package org.cbioportal.security.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class CorsConfig {
    @Value("${security.cors.allowed-origins:}")
    private String allowedOrigins;
    
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        List<String> parsedAllowedOrigins = List.of(allowedOrigins.split(","));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        if ( parsedAllowedOrigins.isEmpty()) {
            return source;
        }
        
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(parsedAllowedOrigins);
        configuration.setAllowedMethods(List.of("GET","POST", "HEAD","OPTIONS"));
        configuration.setAllowedHeaders(List.of("user-agent", "Origin", "Accept", "X-Requested-With","Content-Type",
                "Access-Control-Request-Method","Access-Control-Request-Headers","Content-Encoding",
            "X-Proxy-User-Agreement", "x-current-url"));
        configuration.setExposedHeaders(List.of("total-count", "sample-count", "elapsed-time"));
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
