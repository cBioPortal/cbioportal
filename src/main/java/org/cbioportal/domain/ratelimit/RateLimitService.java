package org.cbioportal.domain.ratelimit;

import java.time.Clock;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class RateLimitService {

  private static final long MILLIS_PER_MINUTE = Duration.ofMinutes(1).toMillis();

  private final ConcurrentMap<String, TokenBucket> buckets = new ConcurrentHashMap<>();
  private final int requestsPerMinute;
  private final int burstCapacity;
  private final Clock clock;

  public RateLimitService(int requestsPerMinute, int burstCapacity) {
    this(requestsPerMinute, burstCapacity, Clock.systemUTC());
  }

  RateLimitService(int requestsPerMinute, int burstCapacity, Clock clock) {
    if (requestsPerMinute <= 0) {
      throw new IllegalArgumentException("rate-limit.requests-per-minute must be positive");
    }
    if (burstCapacity <= 0) {
      throw new IllegalArgumentException("rate-limit.burst-capacity must be positive");
    }
    this.requestsPerMinute = requestsPerMinute;
    this.burstCapacity = burstCapacity;
    this.clock = clock;
  }

  public RateLimitDecision tryConsume(String clientId) {
    return buckets
        .computeIfAbsent(clientId, ignored -> new TokenBucket(burstCapacity, clock.millis()))
        .tryConsume(clock.millis(), requestsPerMinute);
  }

  private static class TokenBucket {

    private final int capacity;
    private double tokens;
    private long lastRefillMillis;

    TokenBucket(int capacity, long nowMillis) {
      this.capacity = capacity;
      this.tokens = capacity;
      this.lastRefillMillis = nowMillis;
    }

    synchronized RateLimitDecision tryConsume(long nowMillis, int requestsPerMinute) {
      long elapsedMillis = Math.max(0, nowMillis - lastRefillMillis);
      tokens =
          Math.min(
              capacity, tokens + (double) elapsedMillis * requestsPerMinute / MILLIS_PER_MINUTE);
      lastRefillMillis = nowMillis;

      if (tokens >= 1) {
        tokens--;
        return RateLimitDecision.permitted();
      }

      long millisUntilNextToken =
          (long) Math.ceil((1 - tokens) * MILLIS_PER_MINUTE / requestsPerMinute);
      long retryAfterSeconds = Math.max(1, (long) Math.ceil(millisUntilNextToken / 1000.0));
      return RateLimitDecision.limited(Duration.ofSeconds(retryAfterSeconds));
    }
  }
}
