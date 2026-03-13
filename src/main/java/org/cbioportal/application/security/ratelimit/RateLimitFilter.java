package org.cbioportal.application.security.ratelimit;

import jakarta.annotation.PreDestroy;
import jakarta.servlet.DispatcherType;
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
  protected boolean shouldNotFilter(HttpServletRequest request) {
    DispatcherType type = request.getDispatcherType();
    // Skip rate limiting for error pages and async dispatches to avoid double-counting
    return type == DispatcherType.ERROR || type == DispatcherType.ASYNC;
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
    TokenBucket bucket = buckets.get(clientId);

    if (bucket == null) {
      if (buckets.size() >= properties.getMaxBuckets()) {
        LOG.warn(
            "Rate limit bucket limit ({}) reached. Rejecting new client '{}' to prevent memory exhaustion.",
            properties.getMaxBuckets(),
            clientId);
        sendErrorResponse(response, 60); // Default 1 min retry if map is full
        return;
      }
      bucket =
          buckets.computeIfAbsent(
              clientId,
              k ->
                  new TokenBucket(
                      properties.getRequestsPerMinute(), properties.getBurstCapacity()));
    }

    if (bucket.tryConsume()) {
      filterChain.doFilter(request, response);
    } else {
      LOG.warn(
          "Rate limit exceeded for client '{}' on {} {}",
          clientId,
          request.getMethod(),
          request.getRequestURI());
      sendErrorResponse(response, bucket.getSecondsUntilRefill());
    }
  }

  private void sendErrorResponse(HttpServletResponse response, long retryAfterSeconds)
      throws IOException {
    response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setHeader("Retry-After", String.valueOf(retryAfterSeconds));
    response.getWriter().write(RATE_LIMIT_RESPONSE);
  }

  /** Shuts down the cleanup executor when the bean is destroyed. */
  @PreDestroy
  public void destroy() {
    LOG.info("Shutting down rate limit cleanup executor");
    cleanupExecutor.shutdown();
    try {
      if (!cleanupExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
        cleanupExecutor.shutdownNow();
      }
    } catch (InterruptedException e) {
      cleanupExecutor.shutdownNow();
      Thread.currentThread().interrupt();
    }
  }

  /**
   * Resolves the client identifier from the request. Uses the {@code X-Forwarded-For} header if
   * {@code trust-proxies} is enabled, otherwise falls back to {@code getRemoteAddr()}.
   *
   * @param request the HTTP request
   * @return the client identifier string
   */
  String resolveClientId(HttpServletRequest request) {
    if (properties.isTrustProxies()) {
      String xForwardedFor = request.getHeader("X-Forwarded-For");
      if (xForwardedFor != null && !xForwardedFor.isBlank()) {
        // Take the first IP in the chain (original client)
        String clientIp = xForwardedFor.split(",")[0].trim();
        if (isValidIp(clientIp)) {
          return clientIp;
        } else {
          LOG.debug("Ignoring invalid X-Forwarded-For client IP: {}", clientIp);
        }
      }
    }
    return request.getRemoteAddr();
  }

  /**
   * Validates that the provided string is a syntactically valid IPv4 or IPv6 address. This prevents
   * using arbitrary header values as client identifiers.
   */
  private boolean isValidIp(String ip) {
    if (ip == null || ip.isBlank()) {
      return false;
    }
    // Simple IPv6 check
    if (ip.contains(":")) {
      return ip.chars()
          .allMatch(
              c ->
                  Character.isDigit(c)
                      || (c >= 'a' && c <= 'f')
                      || (c >= 'A' && c <= 'F')
                      || c == ':');
    }
    // IPv4 validation: four numeric octets, each 0-255
    String[] parts = ip.split("\\.");
    if (parts.length != 4) {
      return false;
    }
    try {
      for (String part : parts) {
        int value = Integer.parseInt(part);
        if (value < 0 || value > 255) return false;
      }
      return true;
    } catch (NumberFormatException e) {
      return false;
    }
  }

  /** Schedules periodic cleanup of stale token buckets to prevent memory leaks. */
  private void scheduleCleanup() {
    long intervalMinutes = properties.getCleanupIntervalMinutes();
    if (intervalMinutes > 0) {
      cleanupExecutor.scheduleAtFixedRate(
          this::cleanupStaleBuckets, intervalMinutes, intervalMinutes, TimeUnit.MINUTES);
    } else {
      LOG.warn(
          "Rate limit cleanup interval is 0; periodic cleanup is disabled. Stale buckets must be cleared manually.");
    }
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
