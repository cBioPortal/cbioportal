package org.cbioportal.legacy.persistence.config;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.regex.Pattern;
import javax.sql.DataSource;
import org.springframework.jdbc.datasource.DelegatingDataSource;

/**
 * Wraps the portal's single JDBC {@link DataSource} so the ClickHouse database it points at can be
 * changed on a running instance, without restarting the app. Until {@link #setDatabase(String)} is
 * called, this behaves exactly like the wrapped {@link DataSource} -- every connection simply keeps
 * connecting to whatever database the delegate {@link DataSource} was configured with.
 *
 * <p>After a switch, every newly obtained {@link Connection} runs a {@code USE <database>}
 * statement before being handed out, so callers never need to know the database changed.
 * Connections already checked out (e.g. for an in-flight transaction) are unaffected until they are
 * returned to the pool and re-borrowed.
 */
public class DynamicDatabaseDataSource extends DelegatingDataSource {

  // \w is exactly [A-Za-z0-9_] under the default (non-UNICODE_CHARACTER_CLASS) flags used here.
  private static final Pattern VALID_DATABASE_NAME = Pattern.compile("\\w+");

  private volatile String database;

  public DynamicDatabaseDataSource(DataSource delegate) {
    super(delegate);
  }

  /** The database name last set via {@link #setDatabase}, or null if never switched. */
  public String getDatabase() {
    return database;
  }

  public void setDatabase(String database) {
    requireValid(database);
    this.database = database;
  }

  /**
   * Confirms {@code database} is reachable through the underlying connection pool, without changing
   * which database this wrapper is currently pointed at. Used to verify a candidate database before
   * committing to a switch, so a bad candidate never becomes visible to any other caller of {@link
   * #getConnection()}.
   */
  public void verifyReachable(String database) throws SQLException {
    requireValid(database);
    try (Connection connection = getTargetDataSource().getConnection();
        Statement statement = connection.createStatement()) {
      statement.execute("USE " + database);
    }
  }

  @Override
  public Connection getConnection() throws SQLException {
    return withActiveDatabase(super.getConnection());
  }

  @Override
  public Connection getConnection(String username, String password) throws SQLException {
    return withActiveDatabase(super.getConnection(username, password));
  }

  private Connection withActiveDatabase(Connection connection) throws SQLException {
    String activeDatabase = database;
    if (activeDatabase != null) {
      // Re-validate immediately before building the dynamic SQL statement, right next to its use,
      // even though setDatabase()/verifyReachable() already validated it -- defense in depth
      // against this field ever being set some other way in the future. USE <db> cannot be
      // parameterized via a JDBC bind variable (identifiers aren't values), so a strict allowlist
      // pattern match is the standard mitigation for this kind of dynamic SQL.
      requireValid(activeDatabase);
      try (Statement statement = connection.createStatement()) {
        statement.execute("USE " + activeDatabase);
      }
    }
    return connection;
  }

  private static void requireValid(String database) {
    if (database == null || !VALID_DATABASE_NAME.matcher(database).matches()) {
      throw new IllegalArgumentException(
          "Invalid database name (must be non-empty and contain only letters, digits and "
              + "underscores): "
              + database);
    }
  }
}
