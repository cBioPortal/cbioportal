package org.cbioportal;

import org.cbioportal.legacy.model.InfoDb;
import org.cbioportal.legacy.service.InfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * Refuses to let the application start against a ClickHouse database whose db_schema_version
 * doesn't match the version this build expects (db.version, from pom.xml), since running with a
 * mismatched schema silently produces wrong queries/results rather than a clear error.
 *
 * <p>Run the migration tool (db-scripts/clickhouse/migrate/migrate_db.py) to bring the database up
 * to date, or set db.suppress_schema_version_mismatch_errors=true to downgrade this to a warning
 * (e.g. when testing a feature unrelated to the schema change in the expected version).
 */
@Component
public class SchemaVersionChecker implements ApplicationRunner {

  private static final Logger logger = LoggerFactory.getLogger(SchemaVersionChecker.class);

  @Autowired private InfoService infoService;

  @Value("${db.version}")
  private String expectedDbSchemaVersion;

  @Value("${db.suppress_schema_version_mismatch_errors:false}")
  private boolean suppressSchemaVersionMismatchErrors;

  @Override
  public void run(ApplicationArguments args) {
    InfoDb infoDb = infoService.getInfoFromDb();
    if (infoDb == null || infoDb.getDbSchemaVersion() == null) {
      failOrWarn(
          "Could not read db_schema_version from the database. The database may not be "
              + "initialized, or the info table's schema_version column is unexpectedly empty.");
      return;
    }

    String actualDbSchemaVersion = infoDb.getDbSchemaVersion();
    if (actualDbSchemaVersion.equals(expectedDbSchemaVersion)) {
      return;
    }

    failOrWarn(
        String.format(
            "Database schema version mismatch: the database is at db_schema_version '%s', but "
                + "this cBioPortal build expects '%s'. Run "
                + "db-scripts/clickhouse/migrate/migrate_db.py against your database to upgrade "
                + "it, or downgrade the cBioPortal build to a version compatible with '%s'.",
            actualDbSchemaVersion, expectedDbSchemaVersion, actualDbSchemaVersion));
  }

  private void failOrWarn(String problem) {
    String message =
        problem
            + " To allow the application to start anyway (which can result in silent errors and "
            + "data distortion), set db.suppress_schema_version_mismatch_errors=true.";

    if (suppressSchemaVersionMismatchErrors) {
      logger.warn(message);
      return;
    }

    throw new SchemaVersionMismatchException(message);
  }

  /** Thrown to abort application startup on an unsuppressed schema version mismatch. */
  public static class SchemaVersionMismatchException extends RuntimeException {
    public SchemaVersionMismatchException(String message) {
      super(message);
    }
  }
}
