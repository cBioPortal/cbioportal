package org.cbioportal.legacy.persistence.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import javax.sql.DataSource;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DynamicDatabaseDataSourceTest {

  private DataSource delegate;
  private Connection connection;
  private Statement statement;
  private DynamicDatabaseDataSource dataSource;

  @Before
  public void setUp() throws Exception {
    delegate = mock(DataSource.class);
    connection = mock(Connection.class);
    statement = mock(Statement.class);
    when(delegate.getConnection()).thenReturn(connection);
    when(connection.createStatement()).thenReturn(statement);
    dataSource = new DynamicDatabaseDataSource(delegate);
  }

  @Test
  public void defaultsToDelegateBehaviorUntilSwitched() throws Exception {
    assertNull(dataSource.getDatabase());
    Connection result = dataSource.getConnection();
    assertEquals(connection, result);
    verify(connection, never()).createStatement();
  }

  @Test
  public void switchesActiveDatabaseOnEveryNewConnection() throws Exception {
    dataSource.setDatabase("cbioportal_v2");
    assertEquals("cbioportal_v2", dataSource.getDatabase());

    dataSource.getConnection();

    verify(statement).execute("USE cbioportal_v2");
  }

  @Test(expected = IllegalArgumentException.class)
  public void rejectsNullDatabaseName() {
    dataSource.setDatabase(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void rejectsBlankDatabaseName() {
    dataSource.setDatabase("");
  }

  @Test(expected = IllegalArgumentException.class)
  public void rejectsDatabaseNameWithInjectionAttempt() {
    dataSource.setDatabase("cbioportal; DROP TABLE cancer_study");
  }

  @Test(expected = IllegalArgumentException.class)
  public void rejectsDatabaseNameWithWhitespace() {
    dataSource.setDatabase("cbioportal v2");
  }

  @Test
  public void verifyReachableDoesNotChangeActiveDatabase() throws Exception {
    dataSource.verifyReachable("cbioportal_v2");

    verify(statement).execute("USE cbioportal_v2");
    assertNull(dataSource.getDatabase());
  }

  @Test(expected = SQLException.class)
  public void verifyReachablePropagatesConnectionFailure() throws Exception {
    when(delegate.getConnection()).thenThrow(new SQLException("no such database"));
    dataSource.verifyReachable("does_not_exist");
  }

  @Test(expected = IllegalArgumentException.class)
  public void verifyReachableRejectsInvalidName() throws Exception {
    dataSource.verifyReachable("bad; name");
  }
}
