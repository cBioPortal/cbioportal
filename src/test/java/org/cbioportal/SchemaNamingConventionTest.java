package org.cbioportal;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.Test;

/**
 * Table and column names in the ClickHouse schema are lower_snake_case. ClickHouse identifiers are
 * case-sensitive, so a column declared {@code displayName} can only ever be written {@code
 * displayName} -- every query, mapper and migration that touches it has to reproduce the casing
 * exactly, and getting it wrong fails at runtime rather than at build time. Keeping one convention
 * removes the question.
 *
 * <p>This checks the shipped schema, the migration script and the test copy of the schema. Two
 * tables predate the convention and are exempted below.
 */
public class SchemaNamingConventionTest {

  // Read from the source tree, not the classpath: pom.xml excludes db-scripts/** from the
  // packaged resources. Surefire runs from the project basedir.
  private static final List<String> SCHEMA_FILES =
      List.of(
          "src/main/resources/db-scripts/clickhouse/init/schema.sql",
          "src/main/resources/db-scripts/clickhouse/migrate/migrate_schema.sql",
          "src/main/resources/db-scripts/clickhouse/populate_derived_tables.sql",
          "src/main/resources/db-scripts/clickhouse/request_log.sql",
          "src/test/resources/schema.sql",
          // Not loaded by this repository: cbioportal-core fetches it by version for its own
          // integration tests. Included here because nothing else checks it, and it drifted.
          "src/test/resources/clickhouse_cgds.sql");

  /**
   * Tables whose column names are not lower_snake_case, with the reason. Nothing may be added here
   * without one -- a new table has no history to inherit.
   *
   * <p>{@code mutation_derived}'s columns are the API's field names on purpose: MutationMapper.xml
   * selects them unaliased and interpolates the caller's sort field straight into {@code ORDER BY
   * "${sortBy}"}, so renaming them would break sorting on the mutation endpoints.
   *
   * <p>{@code mut_sig} is simply old. Its other columns are lower_snake_case and the one mapper
   * that reads these two already aliases them, so renaming them is a migration away rather than a
   * design constraint.
   */
  private static final Map<String, Set<String>> EXEMPT_COLUMNS =
      Map.of(
          "mutation_derived", Set.of("*"),
          "mut_sig", Set.of("numbasescovered", "nummutations"));

  private static final Pattern LOWER_SNAKE_CASE = Pattern.compile("[a-z][a-z0-9_]*");

  private static final Pattern CREATE_TABLE =
      Pattern.compile(
          "CREATE\\s+TABLE\\s+(?:IF\\s+NOT\\s+EXISTS\\s+)?`?([\\w.]+)`?", Pattern.CASE_INSENSITIVE);

  private static final Pattern CREATE_TABLE_WITH_COLUMNS =
      Pattern.compile(
          "CREATE\\s+TABLE\\s+(?:IF\\s+NOT\\s+EXISTS\\s+)?`?([\\w.]+)`?\\s*\\((.*?)\\)\\s*ENGINE",
          Pattern.DOTALL | Pattern.CASE_INSENSITIVE);

  /** First word of a line in a column list that declares something other than a column. */
  private static final Pattern COLUMN = Pattern.compile("^\\s*`?(\\w+)`?\\s", Pattern.MULTILINE);

  private static final Set<String> NOT_A_COLUMN =
      Set.of(
          "index",
          "projection",
          "constraint",
          "primary",
          "order",
          "partition",
          "sample",
          "ttl",
          "settings");

  @Test
  public void tableNamesAreLowerSnakeCase() throws IOException {
    List<String> violations = new ArrayList<>();
    for (String file : SCHEMA_FILES) {
      Matcher matcher = CREATE_TABLE.matcher(stripComments(read(file)));
      while (matcher.find()) {
        String table = matcher.group(1);
        // A name may be database-qualified; both halves have to follow the convention.
        for (String part : table.split("\\.")) {
          if (!LOWER_SNAKE_CASE.matcher(part).matches()) {
            violations.add(file + ": " + table);
          }
        }
      }
    }
    assertThat(violations).as("table names that are not lower_snake_case").isEmpty();
  }

  @Test
  public void columnNamesAreLowerSnakeCase() throws IOException {
    List<String> violations = new ArrayList<>();
    for (String file : SCHEMA_FILES) {
      Matcher table = CREATE_TABLE_WITH_COLUMNS.matcher(stripComments(read(file)));
      while (table.find()) {
        String tableName = unqualified(table.group(1)).toLowerCase(Locale.ROOT);
        Set<String> exempt = EXEMPT_COLUMNS.getOrDefault(tableName, Set.of());
        if (exempt.contains("*")) {
          continue;
        }
        Matcher column = COLUMN.matcher(table.group(2));
        while (column.find()) {
          String name = column.group(1);
          if (NOT_A_COLUMN.contains(name.toLowerCase(Locale.ROOT))
              || exempt.contains(name.toLowerCase(Locale.ROOT))) {
            continue;
          }
          if (!LOWER_SNAKE_CASE.matcher(name).matches()) {
            violations.add(file + ": " + tableName + "." + name);
          }
        }
      }
    }
    assertThat(violations).as("column names that are not lower_snake_case").isEmpty();
  }

  /** Guards the checks above: a regex that stops matching would make them pass on nothing. */
  @Test
  public void everyCreateTableStatementIsChecked() throws IOException {
    for (String file : SCHEMA_FILES) {
      String sql = stripComments(read(file));
      int statements = count(Pattern.compile("CREATE\\s+TABLE", Pattern.CASE_INSENSITIVE), sql);
      assertThat(count(CREATE_TABLE, sql))
          .as("CREATE TABLE statements whose name was read, in %s", file)
          .isEqualTo(statements);
    }
  }

  private static int count(Pattern pattern, String text) {
    Matcher matcher = pattern.matcher(text);
    int found = 0;
    while (matcher.find()) {
      found++;
    }
    assertThat(found).as("matches of %s", pattern.pattern()).isGreaterThan(0);
    return found;
  }

  private static String unqualified(String table) {
    int dot = table.lastIndexOf('.');
    return dot < 0 ? table : table.substring(dot + 1);
  }

  private static String stripComments(String sql) {
    return sql.replaceAll("--[^\n]*", "");
  }

  private static String read(String path) throws IOException {
    Path file = Path.of(path);
    assertThat(Files.exists(file)).as("schema file %s exists", file.toAbsolutePath()).isTrue();
    return Files.readString(file, StandardCharsets.UTF_8);
  }
}
