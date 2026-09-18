package org.cbioportal.application.security.ratelimit;

import org.cbioportal.domain.ratelimit.RateLimitService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

@Configuration
@EnableConfigurationProperties(RateLimitProperties.class)
@ConditionalOnProperty(prefix = "rate-limit", name = "enabled", havingValue = "true")
public class RateLimitConfig {

  @Bean
  public RateLimitService rateLimitService(RateLimitProperties properties) {
    return new RateLimitService(properties.getRequestsPerMinute(), properties.getBurstCapacity());
  }

  @Bean
  public FilterRegistrationBean<RateLimitFilter> rateLimitFilterRegistration(
      RateLimitService rateLimitService) {
    FilterRegistrationBean<RateLimitFilter> registration = new FilterRegistrationBean<>();
    registration.setFilter(new RateLimitFilter(rateLimitService));
    registration.addUrlPatterns("/api/*", "/webservice.do");
    registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
    registration.setName("rateLimitFilter");
    return registration;
  }
}
