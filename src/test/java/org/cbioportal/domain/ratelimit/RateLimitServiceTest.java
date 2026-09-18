package org.cbioportal.domain.ratelimit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class RateLimitServiceTest {

  @Test
  void rejectsAClientAfterItsBurstCapacityIsConsumed() {
    Clock clock = Clock.fixed(Instant.parse("2026-09-18T00:00:00Z"), ZoneOffset.UTC);
    RateLimitService service = new RateLimitService(60, 2, 100, clock);

    assertTrue(service.tryConsume("198.51.100.10").allowed());
    assertTrue(service.tryConsume("198.51.100.10").allowed());

    RateLimitDecision rejected = service.tryConsume("198.51.100.10");

    assertFalse(rejected.allowed());
    assertEquals(Duration.ofSeconds(1), rejected.retryAfter());
  }
}
