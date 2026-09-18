package org.cbioportal.legacy.service.impl;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import org.cbioportal.legacy.persistence.config.DynamicDatabaseDataSource;
import org.cbioportal.legacy.service.CacheService;
import org.cbioportal.legacy.service.DatabaseSwitchService;
import org.cbioportal.legacy.service.exception.CacheOperationException;
import org.cbioportal.legacy.service.exception.DatabaseSwitchException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DatabaseSwitchServiceImpl implements DatabaseSwitchService {

  // Every DataSource bean gets wrapped by DynamicDatabaseDataSourceBeanPostProcessor -- there is
  // the main one plus the file-export feature's separate "exportDataSource". Both must be
  // switched together so exports don't silently keep reading the old database.
  @Autowired private List<DynamicDatabaseDataSource> dynamicDataSources;

  @Autowired private CacheService cacheService;

  @Override
  public String getActiveDatabase() {
    return firstDataSource().getDatabase();
  }

  @Override
  public void switchDatabase(String database)
      throws DatabaseSwitchException, CacheOperationException {
    String previousDatabase = firstDataSource().getDatabase();
    applyDatabase(database);
    try {
      verifyConnectivity();
    } catch (SQLException e) {
      // Don't leave the instance pointed at a database it can't actually reach.
      applyDatabase(previousDatabase);
      throw new DatabaseSwitchException(
          "Could not switch to database '" + database + "': " + e.getMessage(), e);
    }
    cacheService.clearCaches(true);
  }

  private void applyDatabase(String database) {
    for (DynamicDatabaseDataSource dataSource : dynamicDataSources) {
      if (database == null) {
        dataSource.resetToDefault();
      } else {
        dataSource.setDatabase(database);
      }
    }
  }

  private void verifyConnectivity() throws SQLException {
    for (DynamicDatabaseDataSource dataSource : dynamicDataSources) {
      try (Connection connection = dataSource.getConnection()) {
        // getConnection() already ran "USE <database>"; getting here confirms it exists.
      }
    }
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
