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
   * Points every new connection at {@code database} instead, then flushes all caches so nothing
   * read from the previous database lingers. Verifies the new database is reachable before
   * committing to the switch; rolls back and throws {@link DatabaseSwitchException} if not.
   */
  void switchDatabase(String database) throws DatabaseSwitchException, CacheOperationException;
}
