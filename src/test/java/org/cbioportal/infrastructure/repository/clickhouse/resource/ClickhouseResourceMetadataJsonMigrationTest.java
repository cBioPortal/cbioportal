package org.cbioportal.infrastructure.repository.clickhouse.resource;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.cbioportal.infrastructure.repository.clickhouse.AbstractTestcontainers;
import org.junit.Test;
import org.testcontainers.clickhouse.ClickHouseContainer;
import org.testcontainers.containers.Container.ExecResult;
import org.testcontainers.images.builder.Transferable;

/**
 * The 3.2.0 section of migrate_schema.sql, run as migrate_db.py runs it (clickhouse-client with
 * --multiquery and --mutations_sync 2) on a 3.1.0-shaped resource table that already holds rows.
 */
public class ClickhouseResourceMetadataJsonMigrationTest extends AbstractTestcontainers {

  private static final String DB = "migration_3_2_0";

  /** Documents the column must read back as JSONExtractString gives them, key "v" and "w". */
  private static final List<String> DOCUMENTS =
      List.of(
          "{\"v\":\"H&E\",\"w\":\"x\"}",
          "{\"v\":1.50,\"w\":true}",
          "{\"v\":\"2024-1-1\"}",
          "{\"v\":null,\"v\":\"b\"}",
          "{\"v\":\"\",\"v\":\"b\"}",
          "{\"v\":\"a\",\"v\":\"b\"}",
          "{\"v\":{\"z\":1},\"w\":[1,2]}",
          "{\"v\":\"x\",\"v.x\":\"dot\"}",
          "{}",
          "not json",
          "[1,2]");

  private static String section320() throws IOException {
    try (InputStream in =
        AbstractTestcontainers.class
            .getClassLoader()
            .getResourceAsStream("db-scripts/clickhouse/migrate/migrate_schema.sql")) {
      String file = new String(in.readAllBytes(), StandardCharsets.UTF_8);
      int start = file.indexOf("## db_schema_version: 3.2.0");
      assertThat(start).isNotNegative();
      int next = file.indexOf("## db_schema_version:", start + 1);
      String section = next < 0 ? file.substring(start) : file.substring(start, next);
      StringBuilder sql = new StringBuilder();
      for (String line : section.split("\n")) {
        if (!line.startsWith("##")) {
          sql.append(line).append('\n');
        }
      }
      return sql.toString();
    }
  }

  private static ExecResult client(String... args) throws Exception {
    return client(clickhouseContainer, args);
  }

  private static ExecResult client(ClickHouseContainer container, String... args) throws Exception {
    List<String> command =
        new ArrayList<>(
            List.of(
                "clickhouse-client",
                "--user",
                container.getUsername(),
                "--password",
                container.getPassword()));
    command.addAll(List.of(args));
    return container.execInContainer(command.toArray(new String[0]));
  }

  private static String query(String sql) throws Exception {
    ExecResult result = client("-d", DB, "-q", sql);
    assertThat(result.getExitCode()).as(result.getStderr()).isZero();
    return result.getStdout().trim();
  }

  private static ExecResult migrate() throws Exception {
    return migrate(clickhouseContainer);
  }

  private static ExecResult migrate(ClickHouseContainer container) throws Exception {
    return client(
        container,
        "-d",
        DB,
        "--multiquery",
        "--mutations_sync",
        "2",
        "--queries-file",
        "/tmp/m320.sql");
  }

  private static final String RESOURCE_310 =
      "CREATE TABLE resource (`internal_id` Int64, `resource_id` String, `cancer_study_id` Int64,"
          + " `resource_scope` String, `patient_id` Nullable(String), `sample_id`"
          + " Nullable(String), `url` String, `display_name` Nullable(String), `metadata`"
          + " Nullable(String)) ENGINE = MergeTree ORDER BY (cancer_study_id, resource_id,"
          + " patient_id, sample_id, internal_id) SETTINGS allow_nullable_key = 1";

  /**
   * A server whose default profile turns the JSON type off, as a {@code compatibility} before 25.3
   * does on 25.3. The session's SET lets the ALTERs through, but the background mutation runs under
   * the default profile and fails, so the probe has to stop the migration before resource changes.
   */
  @Test
  public void aServerThatCannotWriteTheJsonType_stopsAtTheProbe_andLeavesResourceUnchanged()
      throws Exception {
    try (ClickHouseContainer compat =
        new ClickHouseContainer("clickhouse/clickhouse-server:25.3")
            .withUsername("cbio_user")
            .withPassword("P@ssword1")
            .withCopyToContainer(
                Transferable.of(
                    "<clickhouse><profiles><default><compatibility>24.5</compatibility>"
                        + "</default></profiles></clickhouse>"),
                "/etc/clickhouse-server/users.d/compatibility.xml")) {
      compat.start();
      compat.copyFileToContainer(Transferable.of(section320()), "/tmp/m320.sql");
      assertThat(client(compat, "-q", "CREATE DATABASE " + DB).getExitCode()).isZero();
      assertThat(client(compat, "-d", DB, "-q", RESOURCE_310).getExitCode()).isZero();
      assertThat(
              client(
                      compat,
                      "-d",
                      DB,
                      "-q",
                      "INSERT INTO resource VALUES (1, 'R', 1, 'PATIENT', 'P1', NULL, 'u', NULL,"
                          + " '{\"v\":\"a\"}')")
                  .getExitCode())
          .isZero();

      ExecResult result = migrate(compat);

      assertThat(result.getExitCode()).isNotZero();
      assertThat(result.getStderr())
          .contains("ClickHouse could not write a JSON column in a background mutation");
      assertThat(
              client(
                      compat,
                      "-d",
                      DB,
                      "-q",
                      "SELECT count() FROM system.columns WHERE database = currentDatabase()"
                          + " AND table = 'resource' AND name = 'metadata_json'")
                  .getStdout()
                  .trim())
          .as("resource has no metadata_json column")
          .isEqualTo("0");
    }
  }

  @Test
  public void migratesA310ResourceTable_andItsValuesMatchJsonExtractString() throws Exception {
    clickhouseContainer.copyFileToContainer(Transferable.of(section320()), "/tmp/m320.sql");
    client("-q", "DROP DATABASE IF EXISTS " + DB);
    client("-q", "CREATE DATABASE " + DB);
    query(RESOURCE_310);
    // One insert per document, so the rows sit in several parts, plus bulk rows and a null.
    for (int i = 0; i < DOCUMENTS.size(); i++) {
      query(
          "INSERT INTO resource VALUES ("
              + i
              + ", 'R', 1, 'PATIENT', 'P"
              + i
              + "', NULL, 'u', NULL, '"
              + DOCUMENTS.get(i).replace("\\", "\\\\").replace("'", "\\'")
              + "')");
    }
    query(
        "INSERT INTO resource SELECT 1000 + number, 'R', 1, 'PATIENT', concat('B', toString(number)),"
            + " NULL, 'u', NULL, concat('{\"v\":\"', toString(number % 7), '\",\"w\":', toString(number), '}')"
            + " FROM numbers(5000)");
    query("INSERT INTO resource VALUES (9999, 'R', 1, 'PATIENT', 'N', NULL, 'u', NULL, NULL)");

    ExecResult first = migrate();
    assertThat(first.getExitCode()).as(first.getStderr()).isZero();

    String parity =
        "SELECT countIf(JSONExtractString(ifNull(metadata, '{}'), 'v') !="
            + " substring(ifNull(CAST(metadata_json.v, 'Nullable(String)'), ''), 2))"
            + " + countIf(JSONExtractString(ifNull(metadata, '{}'), 'w') !="
            + " substring(ifNull(CAST(metadata_json.w, 'Nullable(String)'), ''), 2))"
            + " + countIf(JSONExtractString(ifNull(metadata, '{}'), 'v.x') !="
            + " substring(ifNull(CAST(metadata_json.`v.x`, 'Nullable(String)'), ''), 2)),"
            + " countIf(substring(ifNull(CAST(metadata_json.v, 'Nullable(String)'), ''), 2) != ''),"
            + " count() FROM resource";
    assertThat(query(parity)).isEqualTo("0\t5006\t5012");
    assertThat(
            query(
                "SELECT substring(CAST(metadata_json.v, 'String'), 2) FROM resource WHERE internal_id = 3"))
        .as("a null before a duplicate key reads as the null")
        .isEmpty();
    assertThat(
            query(
                "SELECT count() FROM (SELECT name, groupArray(column) AS columns FROM"
                    + " system.parts_columns WHERE database = currentDatabase() AND table ="
                    + " 'resource' AND active GROUP BY name) WHERE NOT has(columns,"
                    + " 'metadata_json')"))
        .as("every part of resource holds metadata_json")
        .isEqualTo("0");
    assertThat(query("EXISTS TABLE metadata_json_probe"))
        .as("the probe table is dropped")
        .isEqualTo("0");
    assertThat(
            query(
                "SELECT count() FROM system.mutations WHERE database = currentDatabase() AND"
                    + " NOT is_done"))
        .isEqualTo("0");

    // A probe table left behind by a failed run is dropped before the next probe.
    query("CREATE TABLE metadata_json_probe (`id` UInt8) ENGINE = MergeTree ORDER BY id");

    // The section is idempotent: run again over a migrated table, it changes nothing.
    ExecResult second = migrate();
    assertThat(second.getExitCode()).as(second.getStderr()).isZero();
    assertThat(query(parity)).isEqualTo("0\t5006\t5012");
    assertThat(query("EXISTS TABLE metadata_json_probe")).isEqualTo("0");

    client("-q", "DROP DATABASE " + DB);
  }
}
