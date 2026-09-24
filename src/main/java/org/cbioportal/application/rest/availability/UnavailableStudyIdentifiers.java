package org.cbioportal.application.rest.availability;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.LongSupplier;
import org.cbioportal.legacy.persistence.mybatis.StudyMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Identifiers (study ids, molecular profile and sample list stable ids) owned by studies whose
 * status is not AVAILABLE, mapped to the owning study id.
 *
 * <p>Cached with a short TTL rather than taken from CacheMapUtil, whose snapshot is only rebuilt at
 * startup or on an explicit cache eviction, so a status flip mid-(re)import would go unnoticed.
 */
@Component
public class UnavailableStudyIdentifiers {

  private static final long DEFAULT_TTL_MILLIS = 30000;
  private static final Logger log = LoggerFactory.getLogger(UnavailableStudyIdentifiers.class);

  private final StudyMapper studyMapper;
  private final long ttlMillis;
  private final LongSupplier nowMillis;
  private final ReentrantLock refreshLock = new ReentrantLock();

  private volatile Map<String, String> studyIdByIdentifier = Map.of();
  private volatile long nextRefreshMillis = Long.MIN_VALUE;

  /** Uses a monotonic clock, so a wall-clock jump backwards cannot freeze the snapshot. */
  @Autowired
  public UnavailableStudyIdentifiers(StudyMapper studyMapper) {
    this(studyMapper, DEFAULT_TTL_MILLIS, () -> System.nanoTime() / 1_000_000);
  }

  UnavailableStudyIdentifiers(StudyMapper studyMapper, long ttlMillis, LongSupplier nowMillis) {
    this.studyMapper = studyMapper;
    this.ttlMillis = ttlMillis;
    this.nowMillis = nowMillis;
  }

  /**
   * Returns identifier → owning study id; empty when every study is available.
   *
   * <p>Reloads once the TTL has expired. Only one caller reloads; concurrent callers keep reading
   * the previous snapshot meanwhile. A failed reload keeps the previous snapshot (empty before the
   * first success, i.e. reads are allowed) until the next TTL expiry.
   */
  public Map<String, String> get() {
    if (nowMillis.getAsLong() >= nextRefreshMillis && refreshLock.tryLock()) {
      try {
        long now = nowMillis.getAsLong();
        if (now >= nextRefreshMillis) {
          try {
            studyIdByIdentifier = toMap(studyMapper.getUnavailableStudyIdentifiers());
          } catch (RuntimeException e) {
            log.warn("Could not refresh unavailable study identifiers: {}", e.toString());
          }
          nextRefreshMillis = now + ttlMillis;
        }
      } finally {
        refreshLock.unlock();
      }
    }
    return studyIdByIdentifier;
  }

  private static Map<String, String> toMap(List<Map<String, String>> rows) {
    Map<String, String> result = new HashMap<>();
    for (Map<String, String> row : rows) {
      String identifier = row.get("identifier");
      String studyId = row.get("studyId");
      if (identifier != null && studyId != null) {
        result.put(identifier, studyId);
      }
    }
    return Map.copyOf(result);
  }
}
