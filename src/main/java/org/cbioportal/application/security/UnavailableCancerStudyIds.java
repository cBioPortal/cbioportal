package org.cbioportal.application.security;

import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.LongSupplier;
import org.cbioportal.legacy.persistence.mybatis.StudyMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

// Short-TTL poll rather than CacheMapUtil, whose snapshot is only rebuilt at startup or on an
// explicit cache eviction, so a status flip mid-(re)import would otherwise go unnoticed.
@Component
public class UnavailableCancerStudyIds {

  private static final long DEFAULT_TTL_MILLIS = 2000;
  private static final Logger log = LoggerFactory.getLogger(UnavailableCancerStudyIds.class);

  private final StudyMapper studyMapper;
  private final long ttlMillis;
  private final LongSupplier nowMillis;
  private final ReentrantLock refreshLock = new ReentrantLock();

  private volatile Set<String> unavailableIds = Set.of();
  private volatile long nextRefreshMillis = Long.MIN_VALUE;

  @Autowired
  public UnavailableCancerStudyIds(StudyMapper studyMapper) {
    // Monotonic: a wall-clock jump backwards must not freeze the snapshot.
    this(studyMapper, DEFAULT_TTL_MILLIS, () -> System.nanoTime() / 1_000_000);
  }

  UnavailableCancerStudyIds(StudyMapper studyMapper, long ttlMillis, LongSupplier nowMillis) {
    this.studyMapper = studyMapper;
    this.ttlMillis = ttlMillis;
    this.nowMillis = nowMillis;
  }

  public boolean isUnavailable(String cancerStudyIdentifier) {
    // Only one thread refreshes; the rest keep reading the previous snapshot meanwhile.
    if (nowMillis.getAsLong() >= nextRefreshMillis && refreshLock.tryLock()) {
      try {
        long now = nowMillis.getAsLong();
        if (now >= nextRefreshMillis) {
          try {
            unavailableIds = Set.copyOf(studyMapper.getUnavailableStudyIds());
          } catch (RuntimeException e) {
            // Keep the previous snapshot (empty before the first success, i.e. allow reads).
            log.warn("Could not refresh unavailable cancer studies: {}", e.toString());
          }
          nextRefreshMillis = now + ttlMillis;
        }
      } finally {
        refreshLock.unlock();
      }
    }
    return unavailableIds.contains(cancerStudyIdentifier);
  }
}
