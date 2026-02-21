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
    this.requestsPerMinute = requestsPerMinute;
  }

  public int getBurstCapacity() {
    return burstCapacity;
  }

  public void setBurstCapacity(int burstCapacity) {
    this.burstCapacity = burstCapacity;
  }

  public int getCleanupIntervalMinutes() {
    return cleanupIntervalMinutes;
  }

  public void setCleanupIntervalMinutes(int cleanupIntervalMinutes) {
    this.cleanupIntervalMinutes = cleanupIntervalMinutes;
  }
}
