package org.cbioportal.legacy.persistence.cachemaputil;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Map;
import org.cbioportal.legacy.model.CancerStudy;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

@RunWith(MockitoJUnitRunner.class)
public class CancerStudyPermissionCacheTest {

  private static final long TTL_SECONDS = 60;

  @Mock private CacheMapBuilder cacheMapBuilder;

  private MutableClock clock;
  private CancerStudyPermissionCache cache;

  @Before
  public void setUp() {
    cache = new CancerStudyPermissionCache(cacheMapBuilder);
    ReflectionTestUtils.setField(cache, "ttlSeconds", TTL_SECONDS);
    clock = new MutableClock(Instant.parse("2026-01-01T00:00:00Z"));
    ReflectionTestUtils.setField(cache, "clock", clock);
  }

  private CancerStudy study(String identifier) {
    CancerStudy cancerStudy = new CancerStudy();
    cancerStudy.setCancerStudyIdentifier(identifier);
    cancerStudy.setGroups("PUBLIC");
    return cancerStudy;
  }

  @Test
  public void buildsOnFirstAccess() {
    Mockito.when(cacheMapBuilder.buildCancerStudyPermissionMap())
        .thenReturn(Map.of("study1", study("study1")));

    Map<String, CancerStudy> result = cache.getCancerStudyPermissionMap();

    Assert.assertEquals(1, result.size());
    Mockito.verify(cacheMapBuilder, Mockito.times(1)).buildCancerStudyPermissionMap();
  }

  @Test
  public void doesNotRebuildWithinTtl() {
    Mockito.when(cacheMapBuilder.buildCancerStudyPermissionMap())
        .thenReturn(Map.of("study1", study("study1")))
        .thenReturn(Map.of("study1", study("study1"), "study2", study("study2")));

    cache.getCancerStudyPermissionMap();
    clock.advanceSeconds(TTL_SECONDS - 1);
    Map<String, CancerStudy> result = cache.getCancerStudyPermissionMap();

    Assert.assertEquals(1, result.size());
    Mockito.verify(cacheMapBuilder, Mockito.times(1)).buildCancerStudyPermissionMap();
  }

  @Test
  public void rebuildsAfterTtlExpires() {
    Mockito.when(cacheMapBuilder.buildCancerStudyPermissionMap())
        .thenReturn(Map.of("study1", study("study1")))
        .thenReturn(Map.of("study1", study("study1"), "study2", study("study2")));

    cache.getCancerStudyPermissionMap();
    clock.advanceSeconds(TTL_SECONDS);
    Map<String, CancerStudy> result = cache.getCancerStudyPermissionMap();

    Assert.assertEquals(2, result.size());
    Mockito.verify(cacheMapBuilder, Mockito.times(2)).buildCancerStudyPermissionMap();
  }

  @Test
  public void invalidateForcesImmediateRebuildRegardlessOfTtl() {
    Mockito.when(cacheMapBuilder.buildCancerStudyPermissionMap())
        .thenReturn(Map.of("study1", study("study1")))
        .thenReturn(Map.of("study1", study("study1"), "study2", study("study2")));

    cache.getCancerStudyPermissionMap();
    cache.invalidate();
    Map<String, CancerStudy> result = cache.getCancerStudyPermissionMap();

    Assert.assertEquals(2, result.size());
    Mockito.verify(cacheMapBuilder, Mockito.times(2)).buildCancerStudyPermissionMap();
  }

  /** A mutable Clock for deterministic TTL testing without real sleeps. */
  private static class MutableClock extends Clock {
    private Instant instant;

    MutableClock(Instant instant) {
      this.instant = instant;
    }

    void advanceSeconds(long seconds) {
      instant = instant.plusSeconds(seconds);
    }

    @Override
    public ZoneOffset getZone() {
      return ZoneOffset.UTC;
    }

    @Override
    public Clock withZone(java.time.ZoneId zone) {
      throw new UnsupportedOperationException();
    }

    @Override
    public Instant instant() {
      return instant;
    }
  }
}
