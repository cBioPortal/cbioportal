package org.cbioportal.application.security.ratelimit;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

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
  public FilterRegistrationBean<RateLimitFilter> rateLimitFilterRegistration(
      RateLimitProperties properties) {
    FilterRegistrationBean<RateLimitFilter> registration = new FilterRegistrationBean<>();
    registration.setFilter(new RateLimitFilter(properties));
    registration.addUrlPatterns("/api/*", "/webservice.do/*");
    // Run early, but after some basic character encoding filters.
    // Ordered.HIGHEST_PRECEDENCE + 1 is generally safe.
    // NOTE:
    // - This is registered as a servlet filter, not a Spring Security filter.
    // - It runs very early in the servlet filter chain (before the Spring Security
    //   filter chain), but there is no strict guarantee about its position relative
    //   to other filters that also use Ordered.HIGHEST_PRECEDENCE.
    // - Because it is not part of the Spring Security filter chain, it will apply
    //   rate limiting to all requests matching the URL patterns above, including
    //   endpoints that are otherwise treated as public/ignored by Spring Security
    //   (for example, health checks or Swagger/OpenAPI endpoints under /api).
    // Administrators should take this into account when configuring monitoring
    // and public endpoints: all matching API endpoints are subject to rate limits.
    registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 1);
    registration.setName("rateLimitFilter");
    return registration;
  }
}
