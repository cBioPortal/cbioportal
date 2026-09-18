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
 * connecting to whatever database is baked into {@code spring.datasource.url}.
 *
 * <p>After a switch, every newly obtained {@link Connection} runs a {@code USE <database>}
 * statement before being handed out, so callers never need to know the database changed.
 * Connections already checked out (e.g. for an in-flight transaction) are unaffected until they are
 * returned to the pool and re-borrowed.
 */
public class DynamicDatabaseDataSource extends DelegatingDataSource {

  private static final Pattern VALID_DATABASE_NAME = Pattern.compile("[A-Za-z0-9_]+");

  private volatile String database;

  public DynamicDatabaseDataSource(DataSource delegate) {
    super(delegate);
  }

  /** The database name last set via {@link #setDatabase}, or null if never switched. */
  public String getDatabase() {
    return database;
  }

  public void setDatabase(String database) {
    if (database == null || !VALID_DATABASE_NAME.matcher(database).matches()) {
      throw new IllegalArgumentException(
          "Invalid database name (must be non-empty and contain only letters, digits and "
              + "underscores): "
              + database);
    }
    this.database = database;
  }

  /** Reverts to the delegate's own default database (whatever spring.datasource.url points at). */
  public void resetToDefault() {
    this.database = null;
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
      try (Statement statement = connection.createStatement()) {
        statement.execute("USE " + activeDatabase);
      }
    }
    return connection;
  }
}
