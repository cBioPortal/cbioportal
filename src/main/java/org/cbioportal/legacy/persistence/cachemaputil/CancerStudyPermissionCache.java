package org.cbioportal.legacy.persistence.cachemaputil;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import org.cbioportal.legacy.model.CancerStudy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;

/**
 * Dedicated, short-TTL, purely in-memory cache for the cancer-study permission map (identifier ->
 * groups; see CacheMapBuilder.buildCancerStudyPermissionMap). This is intentionally separate from
 * CacheMapUtil/the general EhCache/Redis-backed caching mechanism, which is tuned for a much longer
 * TTL: a stale entry here means access changes (including revocations) don't take effect promptly,
 * so this cache expires on its own short timer instead of relying on someone hitting /api/cache.
 *
 * <p>No locking: the underlying query is cheap (join-free), so a rebuild racing across a couple of
 * concurrent requests right at the TTL boundary is harmless -- not worth the complexity of
 * coordinating it.
 */
@Component
// Only meaningful when permission evaluation is actually active -- same condition as
// MethodSecurityConfig/StaticRefCacheMapUtil.
@ConditionalOnExpression(
    "{'oauth2','saml','saml_plus_basic'}.contains('${authenticate}') or ('optional_oauth2' eq '${authenticate}' and 'true' eq '${security.method_authorization_enabled}')")
public class CancerStudyPermissionCache {

  private final CacheMapBuilder cacheMapBuilder;

  @Value("${cache.cancer_study_permission.ttl_seconds:60}")
  private long ttlSeconds;

  private Clock clock = Clock.systemUTC();

  private final AtomicReference<Map<String, CancerStudy>> cachedMap =
      new AtomicReference<>(Collections.emptyMap());
  private volatile Instant lastBuiltAt = Instant.MIN;

  public CancerStudyPermissionCache(CacheMapBuilder cacheMapBuilder) {
    this.cacheMapBuilder = cacheMapBuilder;
  }

  public Map<String, CancerStudy> getCancerStudyPermissionMap() {
    if (isStale()) {
      rebuild();
    }
    return cachedMap.get();
  }

  /** Forces an immediate rebuild, regardless of TTL. */
  public void invalidate() {
    rebuild();
  }

  private void rebuild() {
    // Collections.unmodifiableMap wraps (doesn't copy) the built map, so this stays O(1) --
    // it just makes the published reference's happens-before guarantee actually meaningful, since
    // a caller mutating a shared mutable map after reading it would otherwise defeat it.
    cachedMap.set(Collections.unmodifiableMap(cacheMapBuilder.buildCancerStudyPermissionMap()));
    lastBuiltAt = clock.instant();
  }

  private boolean isStale() {
    return Duration.between(lastBuiltAt, clock.instant()).getSeconds() >= ttlSeconds;
  }
}
