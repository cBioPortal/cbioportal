package org.cbioportal.application.security.ratelimit;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "rate-limit")
public class RateLimitProperties {

  private boolean enabled = false;
  private int requestsPerMinute = 60;
  private int burstCapacity = 10;
  private int maximumBuckets = 10_000;

  public int getMaximumBuckets() {
    return maximumBuckets;
  }

  public void setMaximumBuckets(int maximumBuckets) {
    this.maximumBuckets = maximumBuckets;
  }

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
}
