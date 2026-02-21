package org.cbioportal.application.security.ratelimit;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring configuration for the API rate limiting feature.
 *
 * <p>This configuration is only active when {@code rate.limit.enabled=true} is set in {@code
 * application.properties}. When not enabled, no rate limiting beans are created and the application
 * behaves exactly as before.
 */
@Configuration
@EnableConfigurationProperties(RateLimitProperties.class)
@ConditionalOnProperty(name = "rate.limit.enabled", havingValue = "true")
public class RateLimitConfig {

  @Bean
  public RateLimitFilter rateLimitFilter(RateLimitProperties properties) {
    return new RateLimitFilter(properties);
  }
}
