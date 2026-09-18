package org.cbioportal.legacy.service;

import org.cbioportal.legacy.service.exception.CacheOperationException;
import org.cbioportal.legacy.service.exception.DatabaseSwitchException;

public interface DatabaseSwitchService {

  /**
   * The database currently in use, or null if the portal has never been switched away from the
   * database configured in spring.datasource.url.
   */
  String getActiveDatabase();

  /**
   * Points every new connection at {@code database} instead. Rejects with {@link
   * IllegalArgumentException} unless {@code database} is one of the names configured in {@code
   * database.endpoint.allowed_databases} -- an unset/empty allowlist rejects every switch, so this
   * is opt-in on top of {@code database.endpoint.enabled}, not implied by it. Verifies the new
   * database is reachable before committing to the switch; rolls back and throws {@link
   * DatabaseSwitchException} if not.
   *
   * <p>Also flushes all Spring-managed caches, as a memory-reclaiming courtesy -- correctness does
   * not depend on this flush succeeding or on its timing relative to in-flight requests, since
   * cache keys are scoped by the active database (see {@code CustomKeyGenerator}); entries from a
   * database that's no longer active simply stop being looked up and are reclaimed by TTL or
   * eviction on their own.
   */
  void switchDatabase(String database) throws DatabaseSwitchException, CacheOperationException;
}
