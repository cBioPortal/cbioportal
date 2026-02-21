package org.cbioportal.application.security.ratelimit;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for API rate limiting.
 *
 * <p>Rate limiting is disabled by default. To enable, set {@code rate.limit.enabled=true} in {@code
 * application.properties}.
 *
 * <p>Example configuration:
 *
 * <pre>
 * rate.limit.enabled=true
 * rate.limit.requests-per-minute=120
 * rate.limit.burst-capacity=20
 * rate.limit.cleanup-interval-minutes=60
 * </pre>
 */
@ConfigurationProperties(prefix = "rate.limit")
public class RateLimitProperties {

  /** Whether rate limiting is enabled. Default: false. */
  private boolean enabled = false;

  /** Maximum sustained requests per minute per client. Default: 120. */
  private int requestsPerMinute = 120;

  /** Maximum burst capacity (tokens available for short bursts). Default: 20. */
  private int burstCapacity = 20;

  /** Interval in minutes for cleaning up stale rate limit buckets. Default: 60. */
  private int cleanupIntervalMinutes = 60;

  /**
   * Maximum number of client buckets to track in memory as defense-in-depth against resource
   * exhaustion. Default: 100,000.
   */
  private int maxBuckets = 100000;

  /**
   * Whether to trust X-Forwarded-For headers. Default: false. Only enable this if the application
   * is behind a trusted reverse proxy.
   */
  private boolean trustProxies = false;

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public int getRequestsPerMinute() {
    return requestsPerMinute;
  }

  public void setRequestsPerMinute(int requestsPerMinute) {
    this.requestsPerMinute = Math.max(1, requestsPerMinute);
  }

  public int getBurstCapacity() {
    return burstCapacity;
  }

  public void setBurstCapacity(int burstCapacity) {
    this.burstCapacity = Math.max(1, burstCapacity);
  }

  public int getCleanupIntervalMinutes() {
    return cleanupIntervalMinutes;
  }

  public void setCleanupIntervalMinutes(int cleanupIntervalMinutes) {
    this.cleanupIntervalMinutes = Math.max(0, cleanupIntervalMinutes);
  }

  public int getMaxBuckets() {
    return maxBuckets;
  }

  public void setMaxBuckets(int maxBuckets) {
    this.maxBuckets = Math.max(1, maxBuckets);
  }

  public boolean isTrustProxies() {
    return trustProxies;
  }

  public void setTrustProxies(boolean trustProxies) {
    this.trustProxies = trustProxies;
  }
}
