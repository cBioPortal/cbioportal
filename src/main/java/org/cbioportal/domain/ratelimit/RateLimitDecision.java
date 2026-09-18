package org.cbioportal.domain.ratelimit;

import java.time.Duration;

public record RateLimitDecision(boolean allowed, Duration retryAfter) {

  public static RateLimitDecision permitted() {
    return new RateLimitDecision(true, Duration.ZERO);
  }

  public static RateLimitDecision limited(Duration retryAfter) {
    return new RateLimitDecision(false, retryAfter);
  }
}
