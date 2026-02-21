package org.cbioportal.application.security.ratelimit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
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

    // Wait for refill with a generous buffer (10ms per token at 6000 req/min)
    Thread.sleep(100);

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
    Thread.sleep(50);
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

    // Wait long enough for many tokens to refill (10ms per token * 10 tokens = 100ms)
    // We sleep 200ms to be extra safe on slow CI.
    Thread.sleep(200);

    // Should have at most 3 tokens (burst capacity)
    int consumed = 0;
    while (bucket.tryConsume()) {
      consumed++;
    }
    assertTrue(consumed <= 3, "Should not exceed burst capacity, got " + consumed);
  }

  @Test
  void shouldBeThreadSafeUnderHighContention() throws InterruptedException {
    int numThreads = 10;
    int requestsPerThread = 1000;
    // 60,000 req/min = 1,000 req/sec = 1 token every 1ms
    TokenBucket bucket = new TokenBucket(60000, 100);

    CountDownLatch latch = new CountDownLatch(1);
    AtomicInteger totalConsumed = new AtomicInteger(0);

    Thread[] threads = new Thread[numThreads];
    for (int i = 0; i < numThreads; i++) {
      threads[i] =
          new Thread(
              () -> {
                try {
                  latch.await();
                  for (int j = 0; j < requestsPerThread; j++) {
                    if (bucket.tryConsume()) {
                      totalConsumed.incrementAndGet();
                    }
                    // Small sleep to allow refills to happen mid-test
                    if (j % 50 == 0) Thread.sleep(1);
                  }
                } catch (InterruptedException e) {
                  Thread.currentThread().interrupt();
                }
              });
      threads[i].start();
    }

    long start = System.currentTimeMillis();
    latch.countDown();
    for (Thread t : threads) {
      t.join();
    }
    long end = System.currentTimeMillis();
    long duration = end - start;

    // We expect some tokens to be consumed.
    // Initial burst (100) + refills over duration (duration * 1 token/ms)
    int maxPossible = 100 + (int) duration + 50; // +50 buffer for OS scheduling
    int actual = totalConsumed.get();

    assertTrue(actual > 100, "Should have consumed more than the initial burst");
    assertTrue(
        actual <= maxPossible,
        "Should not exceed theoretical maximum tokens: " + actual + " vs " + maxPossible);
  }
}
