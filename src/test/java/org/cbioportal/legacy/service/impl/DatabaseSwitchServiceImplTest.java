package org.cbioportal.legacy.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.sql.DataSource;
import org.cbioportal.legacy.persistence.config.DynamicDatabaseDataSource;
import org.cbioportal.legacy.service.CacheService;
import org.cbioportal.legacy.service.exception.DatabaseSwitchException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

@RunWith(MockitoJUnitRunner.class)
public class DatabaseSwitchServiceImplTest {

  @InjectMocks private DatabaseSwitchServiceImpl service;

  @Mock private CacheService cacheService;

  private DynamicDatabaseDataSource mainDataSource;
  private DynamicDatabaseDataSource exportDataSource;

  @Before
  public void setUp() throws Exception {
    mainDataSource = new DynamicDatabaseDataSource(workingDelegate());
    exportDataSource = new DynamicDatabaseDataSource(workingDelegate());
    ReflectionTestUtils.setField(
        service, "dynamicDataSources", List.of(mainDataSource, exportDataSource));
    // @PostConstruct doesn't run under @InjectMocks -- seed the allowlist directly with every
    // name the tests below switch to.
    ReflectionTestUtils.setField(
        service,
        "allowedDatabases",
        Set.of("cbioportal_v2", "does_not_exist", "not valid; drop table"));
  }

  private static DataSource workingDelegate() throws SQLException {
    DataSource delegate = mock(DataSource.class);
    Connection connection = mock(Connection.class);
    Statement statement = mock(Statement.class);
    when(delegate.getConnection()).thenReturn(connection);
    when(connection.createStatement()).thenReturn(statement);
    return delegate;
  }

  @Test
  public void switchDatabaseUpdatesEveryDataSourceAndFlushesCaches() throws Exception {
    service.switchDatabase("cbioportal_v2");

    assertEquals("cbioportal_v2", service.getActiveDatabase());
    assertEquals("cbioportal_v2", mainDataSource.getDatabase());
    assertEquals("cbioportal_v2", exportDataSource.getDatabase());
    verify(cacheService, times(1)).clearCaches(true);
  }

  @Test(expected = IllegalArgumentException.class)
  public void switchDatabaseRejectsInvalidName() throws Exception {
    service.switchDatabase("not valid; drop table");
  }

  @Test
  public void switchDatabaseNeverAppliesWhenNewDatabaseIsUnreachable() throws Exception {
    DataSource failingDelegate = mock(DataSource.class);
    when(failingDelegate.getConnection()).thenThrow(new SQLException("no such database"));
    DynamicDatabaseDataSource failing = new DynamicDatabaseDataSource(failingDelegate);
    ReflectionTestUtils.setField(failing, "database", "cbioportal");
    ReflectionTestUtils.setField(service, "dynamicDataSources", List.of(failing));

    try {
      service.switchDatabase("does_not_exist");
      fail("expected DatabaseSwitchException");
    } catch (DatabaseSwitchException expected) {
      // expected
    }

    // Connectivity is verified before anything is applied, so a failed switch never touches the
    // active database in the first place -- there is nothing to roll back.
    assertEquals("cbioportal", failing.getDatabase());
    verify(cacheService, never()).clearCaches(true);
  }

  @Test(expected = IllegalStateException.class)
  public void failsClearlyWhenNoDataSourcesFound() {
    ReflectionTestUtils.setField(service, "dynamicDataSources", Collections.emptyList());
    service.getActiveDatabase();
  }

  @Test
  public void switchDatabaseIsNoOpWhenAlreadyOnRequestedDatabase() throws Exception {
    ReflectionTestUtils.setField(mainDataSource, "database", "cbioportal_v2");
    ReflectionTestUtils.setField(exportDataSource, "database", "cbioportal_v2");

    service.switchDatabase("cbioportal_v2");

    verify(cacheService, never()).clearCaches(true);
  }

  @Test
  public void switchDatabaseRejectsDatabaseNotInAllowlist() throws Exception {
    ReflectionTestUtils.setField(service, "allowedDatabases", Set.of("cbioportal_v2"));

    try {
      service.switchDatabase("some_other_db");
      fail("expected IllegalArgumentException");
    } catch (IllegalArgumentException expected) {
      // expected
    }

    assertEquals(null, mainDataSource.getDatabase());
    verify(cacheService, never()).clearCaches(true);
  }

  @Test
  public void switchDatabaseRejectsEverythingWhenAllowlistIsEmpty() throws Exception {
    ReflectionTestUtils.setField(service, "allowedDatabases", Set.of());

    try {
      service.switchDatabase("cbioportal_v2");
      fail("expected IllegalArgumentException");
    } catch (IllegalArgumentException expected) {
      // expected
    }
  }

  @Test
  public void initParsesCommaSeparatedAllowlistTrimmingWhitespaceAndBlanks() {
    DatabaseSwitchServiceImpl realService =
        new DatabaseSwitchServiceImpl(Collections.emptyList(), null);
    ReflectionTestUtils.setField(
        realService, "allowedDatabasesCsv", " cbioportal , cbioportal_v2,,  ");

    ReflectionTestUtils.invokeMethod(realService, "init");

    assertEquals(
        Set.of("cbioportal", "cbioportal_v2"),
        ReflectionTestUtils.getField(realService, "allowedDatabases"));
  }

  @Test
  public void initProducesEmptyAllowlistWhenPropertyIsUnset() {
    DatabaseSwitchServiceImpl realService =
        new DatabaseSwitchServiceImpl(Collections.emptyList(), null);
    ReflectionTestUtils.setField(realService, "allowedDatabasesCsv", "");

    ReflectionTestUtils.invokeMethod(realService, "init");

    assertEquals(Set.of(), ReflectionTestUtils.getField(realService, "allowedDatabases"));
  }
}
