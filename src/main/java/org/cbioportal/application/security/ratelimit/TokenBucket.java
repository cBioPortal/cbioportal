package org.cbioportal.application.security.ratelimit;

import java.util.concurrent.atomic.AtomicLong;

/**
 * A thread-safe token bucket implementation for rate limiting.
 *
 * <p>Tokens are refilled at a steady rate up to a maximum capacity. Each request consumes one
 * token. When no tokens are available, the request is rate-limited.
 */
public class TokenBucket {

  private final int maxTokens;
  private final long refillIntervalNanos;
  private final AtomicLong availableTokens;
  private final AtomicLong lastRefillTimestamp;
  private final AtomicLong lastAccessTimestamp;

  /**
   * Creates a new TokenBucket.
   *
   * @param requestsPerMinute the maximum sustained request rate
   * @param burstCapacity the maximum number of tokens (burst allowance)
   */
  public TokenBucket(int requestsPerMinute, int burstCapacity) {
    this.maxTokens = burstCapacity;
    this.refillIntervalNanos =
        (requestsPerMinute > 0) ? (60L * 1_000_000_000L) / requestsPerMinute : Long.MAX_VALUE;
    this.availableTokens = new AtomicLong(burstCapacity);
    this.lastRefillTimestamp = new AtomicLong(System.nanoTime());
    this.lastAccessTimestamp = new AtomicLong(System.currentTimeMillis());
  }

  /**
   * Attempts to consume a single token from the bucket.
   *
   * @return {@code true} if a token was available and consumed, {@code false} if rate-limited
   */
  public boolean tryConsume() {
    refill();
    lastAccessTimestamp.set(System.currentTimeMillis());
    long currentTokens = availableTokens.get();
    while (currentTokens > 0) {
      if (availableTokens.compareAndSet(currentTokens, currentTokens - 1)) {
        return true;
      }
      currentTokens = availableTokens.get();
    }
    return false;
  }

  /**
   * Returns the estimated number of seconds until the next token becomes available.
   *
   * @return seconds until next refill, minimum 1
   */
  public long getSecondsUntilRefill() {
    long nanosUntilRefill = refillIntervalNanos - (System.nanoTime() - lastRefillTimestamp.get());
    return Math.max(1, nanosUntilRefill / 1_000_000_000L);
  }

  /**
   * Returns the last time this bucket was accessed, in milliseconds since epoch.
   *
   * @return last access timestamp in milliseconds
   */
  public long getLastAccessTimeMillis() {
    return lastAccessTimestamp.get();
  }

  /** Refills tokens based on elapsed time since the last refill. */
  private void refill() {
    long now = System.nanoTime();
    long last = lastRefillTimestamp.get();
    long elapsed = now - last;
    long tokensToAdd = elapsed / refillIntervalNanos;
    if (tokensToAdd > 0) {
      if (lastRefillTimestamp.compareAndSet(last, last + tokensToAdd * refillIntervalNanos)) {
        long currentTokens = availableTokens.get();
        long newTokens = Math.min(maxTokens, currentTokens + tokensToAdd);
        availableTokens.set(newTokens);
      }
    }
  }
}
