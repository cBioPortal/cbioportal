package org.cbioportal.application.security.ratelimit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class TokenBucketTest {

  @Test
  void shouldAllowRequestsWithinLimit() {
    TokenBucket bucket = new TokenBucket(60, 5);

    for (int i = 0; i < 5; i++) {
      assertTrue(bucket.tryConsume(), "Request " + (i + 1) + " should be allowed");
    }
  }

  @Test
  void shouldRejectRequestsOverBurstCapacity() {
    TokenBucket bucket = new TokenBucket(60, 3);

    assertTrue(bucket.tryConsume());
    assertTrue(bucket.tryConsume());
    assertTrue(bucket.tryConsume());
    assertFalse(bucket.tryConsume(), "4th request should be rate-limited");
  }

  @Test
  void shouldRefillTokensOverTime() throws InterruptedException {
    // 6000 requests per minute = 100 per second = 1 token every 10ms
    TokenBucket bucket = new TokenBucket(6000, 1);

    assertTrue(bucket.tryConsume(), "First request should be allowed");
    assertFalse(bucket.tryConsume(), "Second request should be rate-limited (no tokens)");

    // Wait for refill (at least 10ms for one token at 6000 req/min)
    Thread.sleep(20);

    assertTrue(bucket.tryConsume(), "Request after refill should be allowed");
  }

  @Test
  void shouldReturnPositiveSecondsUntilRefill() {
    TokenBucket bucket = new TokenBucket(60, 1);

    bucket.tryConsume(); // exhaust tokens
    long retryAfter = bucket.getSecondsUntilRefill();

    assertTrue(retryAfter >= 0, "Retry-After should be non-negative");
  }

  @Test
  void shouldTrackLastAccessTime() throws InterruptedException {
    TokenBucket bucket = new TokenBucket(60, 5);

    long beforeAccess = System.currentTimeMillis();
    Thread.sleep(5);
    bucket.tryConsume();
    long afterAccess = System.currentTimeMillis();

    assertTrue(bucket.getLastAccessTimeMillis() >= beforeAccess);
    assertTrue(bucket.getLastAccessTimeMillis() <= afterAccess);
  }

  @Test
  void shouldNotExceedMaxTokensOnRefill() throws InterruptedException {
    // 6000 requests per minute, burst capacity of 3
    TokenBucket bucket = new TokenBucket(6000, 3);

    // Exhaust all tokens
    bucket.tryConsume();
    bucket.tryConsume();
    bucket.tryConsume();
    assertFalse(bucket.tryConsume());

    // Wait long enough for many tokens to refill
    Thread.sleep(100);

    // Should have at most 3 tokens (burst capacity)
    int consumed = 0;
    while (bucket.tryConsume()) {
      consumed++;
    }
    assertTrue(consumed <= 3, "Should not exceed burst capacity, got " + consumed);
  }
}
