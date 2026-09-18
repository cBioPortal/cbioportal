package org.cbioportal.legacy.service.impl;

import jakarta.annotation.PostConstruct;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.cbioportal.legacy.persistence.config.DynamicDatabaseDataSource;
import org.cbioportal.legacy.service.CacheService;
import org.cbioportal.legacy.service.DatabaseSwitchService;
import org.cbioportal.legacy.service.exception.CacheOperationException;
import org.cbioportal.legacy.service.exception.DatabaseSwitchException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class DatabaseSwitchServiceImpl implements DatabaseSwitchService {

  // Every DataSource bean gets wrapped by DynamicDatabaseDataSourceBeanPostProcessor -- there is
  // the main one plus the file-export feature's separate "exportDataSource". Both must be
  // switched together so exports don't silently keep reading the old database.
  private final List<DynamicDatabaseDataSource> dynamicDataSources;

  private final CacheService cacheService;

  // Comma-separated allowlist of database names that switchDatabase() may target. Unset/empty
  // means no database is allowed -- switching requires explicitly opting in, not just enabling
  // the endpoint.
  @Value("${database.endpoint.allowed_databases:}")
  private String allowedDatabasesCsv;

  private Set<String> allowedDatabases;

  public DatabaseSwitchServiceImpl(
      List<DynamicDatabaseDataSource> dynamicDataSources, CacheService cacheService) {
    this.dynamicDataSources = dynamicDataSources;
    this.cacheService = cacheService;
  }

  @PostConstruct
  private void init() {
    allowedDatabases =
        Arrays.stream(allowedDatabasesCsv.split(","))
            .map(String::trim)
            .filter(name -> !name.isEmpty())
            .collect(Collectors.toSet());
  }

  @Override
  public String getActiveDatabase() {
    return firstDataSource().getDatabase();
  }

  @Override
  public void switchDatabase(String database)
      throws DatabaseSwitchException, CacheOperationException {
    if (!allowedDatabases.contains(database)) {
      throw new IllegalArgumentException(
          "Database '"
              + database
              + "' is not in database.endpoint.allowed_databases; refusing to switch.");
    }
    if (Objects.equals(database, getActiveDatabase())) {
      return;
    }
    // Verify every data source can actually reach the candidate database BEFORE touching any
    // shared state, so a bad candidate never becomes visible to a concurrent request -- there is
    // no partially-switched window to roll back from, because nothing is applied until every
    // data source has already confirmed it.
    try {
      for (DynamicDatabaseDataSource dataSource : dynamicDataSources) {
        dataSource.verifyReachable(database);
      }
    } catch (SQLException e) {
      throw new DatabaseSwitchException(
          "Could not switch to database '" + database + "': " + e.getMessage(), e);
    }
    for (DynamicDatabaseDataSource dataSource : dynamicDataSources) {
      dataSource.setDatabase(database);
    }
    cacheService.clearCaches(true);
  }

  private DynamicDatabaseDataSource firstDataSource() {
    if (dynamicDataSources.isEmpty()) {
      throw new IllegalStateException(
          "No DynamicDatabaseDataSource beans found; is "
              + "DynamicDatabaseDataSourceBeanPostProcessor registered?");
    }
    return dynamicDataSources.get(0);
  }
}
