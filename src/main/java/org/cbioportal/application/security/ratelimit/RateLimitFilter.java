package org.cbioportal.application.security.ratelimit;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.cbioportal.domain.ratelimit.RateLimitDecision;
import org.cbioportal.domain.ratelimit.RateLimitService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;

public class RateLimitFilter extends OncePerRequestFilter {

  private static final Logger LOG = LoggerFactory.getLogger(RateLimitFilter.class);

  private final RateLimitService rateLimitService;

  public RateLimitFilter(RateLimitService rateLimitService) {
    this.rateLimitService = rateLimitService;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    String clientId = clientId(request);
    if (clientId == null) {
      filterChain.doFilter(request, response);
      return;
    }
    RateLimitDecision decision = rateLimitService.tryConsume(clientId);
    if (!decision.allowed()) {
      long retryAfterSeconds = decision.retryAfter().toSeconds();
      response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
      response.setHeader("Retry-After", Long.toString(retryAfterSeconds));
      LOG.warn(
          "Rate limit exceeded for client {} on {} {}",
          clientId,
          request.getMethod(),
          request.getRequestURI());
      return;
    }
    filterChain.doFilter(request, response);
  }

  private String clientId(HttpServletRequest request) {
    String remoteAddress = request.getRemoteAddr();
    return remoteAddress == null || remoteAddress.isBlank() ? null : remoteAddress;
  }
}
