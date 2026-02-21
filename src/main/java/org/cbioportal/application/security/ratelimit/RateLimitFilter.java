package org.cbioportal.application.security.ratelimit;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * A rate limiting filter that restricts the number of API requests per client.
 *
 * <p>Uses a token bucket algorithm with per-client buckets identified by IP address. Buckets are
 * stored in-memory and periodically cleaned up to prevent memory leaks.
 *
 * <p>When rate limiting is disabled via configuration, this filter passes all requests through
 * without any processing.
 *
 * @see TokenBucket
 * @see RateLimitProperties
 */
public class RateLimitFilter extends OncePerRequestFilter {

  private static final Logger LOG = LoggerFactory.getLogger(RateLimitFilter.class);
  private static final String RATE_LIMIT_RESPONSE =
      "{\"message\":\"Rate limit exceeded. Please try again later.\"}";

  private final ConcurrentHashMap<String, TokenBucket> buckets = new ConcurrentHashMap<>();
  private final RateLimitProperties properties;
  private final ScheduledExecutorService cleanupExecutor;

  public RateLimitFilter(RateLimitProperties properties) {
    this.properties = properties;
    this.cleanupExecutor =
        Executors.newSingleThreadScheduledExecutor(
            r -> {
              Thread t = new Thread(r, "rate-limit-cleanup");
              t.setDaemon(true);
              return t;
            });
    scheduleCleanup();
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    if (!properties.isEnabled()) {
      filterChain.doFilter(request, response);
      return;
    }

    String clientId = resolveClientId(request);
    TokenBucket bucket =
        buckets.computeIfAbsent(
            clientId,
            k -> new TokenBucket(properties.getRequestsPerMinute(), properties.getBurstCapacity()));

    if (bucket.tryConsume()) {
      filterChain.doFilter(request, response);
    } else {
      LOG.warn(
          "Rate limit exceeded for client '{}' on {} {}",
          clientId,
          request.getMethod(),
          request.getRequestURI());
      long retryAfter = bucket.getSecondsUntilRefill();
      response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
      response.setContentType(MediaType.APPLICATION_JSON_VALUE);
      response.setHeader("Retry-After", String.valueOf(retryAfter));
      response.getWriter().write(RATE_LIMIT_RESPONSE);
    }
  }

  /**
   * Resolves the client identifier from the request. Uses the {@code X-Forwarded-For} header if
   * present (for clients behind a proxy), otherwise falls back to {@code getRemoteAddr()}.
   *
   * @param request the HTTP request
   * @return the client identifier string
   */
  String resolveClientId(HttpServletRequest request) {
    String xForwardedFor = request.getHeader("X-Forwarded-For");
    if (xForwardedFor != null && !xForwardedFor.isBlank()) {
      // Take the first IP in the chain (original client)
      return xForwardedFor.split(",")[0].trim();
    }
    return request.getRemoteAddr();
  }

  /** Schedules periodic cleanup of stale token buckets to prevent memory leaks. */
  private void scheduleCleanup() {
    long intervalMinutes = properties.getCleanupIntervalMinutes();
    cleanupExecutor.scheduleAtFixedRate(
        this::cleanupStaleBuckets, intervalMinutes, intervalMinutes, TimeUnit.MINUTES);
  }

  /** Removes token buckets that have not been accessed within the cleanup interval. */
  void cleanupStaleBuckets() {
    long staleThresholdMillis = TimeUnit.MINUTES.toMillis(properties.getCleanupIntervalMinutes());
    long now = System.currentTimeMillis();
    int removed = 0;
    Iterator<Map.Entry<String, TokenBucket>> iterator = buckets.entrySet().iterator();
    while (iterator.hasNext()) {
      Map.Entry<String, TokenBucket> entry = iterator.next();
      if (now - entry.getValue().getLastAccessTimeMillis() > staleThresholdMillis) {
        iterator.remove();
        removed++;
      }
    }
    if (removed > 0) {
      LOG.info("Rate limit cleanup: removed {} stale client buckets", removed);
    }
  }

  /** Returns the current number of tracked client buckets. Visible for testing. */
  int getBucketCount() {
    return buckets.size();
  }
}
