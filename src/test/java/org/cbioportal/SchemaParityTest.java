package org.cbioportal;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.Test;

/**
 * The integration tests run against their own copy of the schema ({@code
 * src/test/resources/schema.sql}) rather than the one production installs use ({@code
 * db-scripts/clickhouse/init/schema.sql}). Nothing kept the two in step, so the copy drifted: it
 * declared {@code resource_data.CANCER_STUDY_ID} as {@code Int64} where production had {@code
 * Int32}, and it went on creating {@code resource_sample} / {@code resource_patient} / {@code
 * resource_study} after production stopped. That second drift actively hid a bug -- a
 * resource-count query was still reading those tables, and only the test schema's stale copies let
 * it pass.
 *
 * <p>This asserts the two define the same tables, columns and engines, so the next drift fails here
 * instead of hiding something. It compares structure only: comments, whitespace, statement order
 * and backticks are normalised away.
 *
 * <p>The real fix is to delete the duplicate and test against the production schema directly. Until
 * then, this keeps them honest.
 */
public class SchemaParityTest {

  // Read from the source tree, not the classpath: pom.xml excludes db-scripts/** from the
  // packaged resources, so the production schema is not on it. Surefire runs from the project
  // basedir.
  private static final String POM = "pom.xml";
  private static final String PRODUCTION =
      "src/main/resources/db-scripts/clickhouse/init/schema.sql";
  private static final String TEST_COPY = "src/test/resources/schema.sql";

  private static final Pattern SEEDED_VERSION =
      Pattern.compile(
          "INSERT INTO info\\s*\\([^)]*\\)\\s*VALUES\\s*\\(\\s*'([^']+)'",
          Pattern.CASE_INSENSITIVE);

  private static final Pattern POM_DB_VERSION =
      Pattern.compile("<db\\.version>([^<]+)</db\\.version>");

  private static final Pattern CREATE_TABLE =
      Pattern.compile(
          "CREATE TABLE\\s+(?:IF NOT EXISTS\\s+)?(\\w+)\\s*\\((.*?)\\)\\s*ENGINE\\s*=\\s*([^;]*);",
          Pattern.DOTALL | Pattern.CASE_INSENSITIVE);

  @Test
  public void testSchemaMatchesProductionSchema() throws IOException {
    Map<String, String> production = parse(PRODUCTION);
    Map<String, String> testCopy = parse(TEST_COPY);

    assertThat(testCopy.keySet())
        .as("tables defined in %s but not %s (or vice versa)", TEST_COPY, PRODUCTION)
        .containsExactlyInAnyOrderElementsOf(production.keySet());

    for (Map.Entry<String, String> table : production.entrySet()) {
      assertThat(testCopy.get(table.getKey()))
          .as("definition of table '%s' in %s", table.getKey(), TEST_COPY)
          .isEqualTo(table.getValue());
    }
  }

  /**
   * A fresh install seeds info.db_schema_version from schema.sql, and {@link
   * org.cbioportal.SchemaVersionChecker} refuses to start the application when that value does not
   * match db.version from pom.xml. Bumping the migration without bumping the seed therefore breaks
   * every fresh install -- the portal will not boot -- while upgrades from an older database keep
   * working, so it is easy to miss.
   */
  @Test
  public void seededSchemaVersionMatchesTheVersionTheBuildExpects() throws IOException {
    String expected = match(POM_DB_VERSION, read(POM), "db.version in " + POM);

    assertThat(match(SEEDED_VERSION, read(PRODUCTION), "seeded db_schema_version in " + PRODUCTION))
        .as("%s seeds a db_schema_version the build would refuse to start against", PRODUCTION)
        .isEqualTo(expected);

    assertThat(match(SEEDED_VERSION, read(TEST_COPY), "seeded db_schema_version in " + TEST_COPY))
        .as("%s seeds a different db_schema_version from production", TEST_COPY)
        .isEqualTo(expected);
  }

  private static String match(Pattern pattern, String text, String what) {
    Matcher matcher = pattern.matcher(text);
    assertThat(matcher.find()).as("found %s", what).isTrue();
    return matcher.group(1).trim();
  }

  /** Table name to normalised "columns | engine" definition. */
  private static Map<String, String> parse(String resource) throws IOException {
    String sql = read(resource).replaceAll("--[^\n]*", "");
    Map<String, String> tables = new LinkedHashMap<>();
    Matcher matcher = CREATE_TABLE.matcher(sql);
    while (matcher.find()) {
      String columns = normalise(matcher.group(2)).replace("`", "");
      String engine = normalise(matcher.group(3));
      tables.put(matcher.group(1).toLowerCase(), columns + " | " + engine);
    }
    assertThat(tables).as("tables parsed from %s", resource).isNotEmpty();
    return tables;
  }

  private static String normalise(String sql) {
    return sql.replaceAll("\\s+", " ").trim();
  }

  private static String read(String path) throws IOException {
    Path file = Path.of(path);
    assertThat(Files.exists(file)).as("schema file %s exists", file.toAbsolutePath()).isTrue();
    return Files.readString(file, StandardCharsets.UTF_8);
  }
}
