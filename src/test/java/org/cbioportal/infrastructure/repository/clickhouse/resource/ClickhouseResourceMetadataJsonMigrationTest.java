package org.cbioportal.infrastructure.repository.clickhouse.resource;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.cbioportal.infrastructure.repository.clickhouse.AbstractTestcontainers;
import org.junit.Test;
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
    List<String> command =
        new ArrayList<>(
            List.of(
                "clickhouse-client",
                "--user",
                clickhouseContainer.getUsername(),
                "--password",
                clickhouseContainer.getPassword()));
    command.addAll(List.of(args));
    return clickhouseContainer.execInContainer(command.toArray(new String[0]));
  }

  private static String query(String sql) throws Exception {
    ExecResult result = client("-d", DB, "-q", sql);
    assertThat(result.getExitCode()).as(result.getStderr()).isZero();
    return result.getStdout().trim();
  }

  private static ExecResult migrate() throws Exception {
    return client(
        "-d", DB, "--multiquery", "--mutations_sync", "2", "--queries-file", "/tmp/m320.sql");
  }

  @Test
  public void migratesA310ResourceTable_andItsValuesMatchJsonExtractString() throws Exception {
    clickhouseContainer.copyFileToContainer(Transferable.of(section320()), "/tmp/m320.sql");
    client("-q", "DROP DATABASE IF EXISTS " + DB);
    client("-q", "CREATE DATABASE " + DB);
    query(
        "CREATE TABLE resource (`internal_id` Int64, `resource_id` String, `cancer_study_id` Int64,"
            + " `resource_scope` String, `patient_id` Nullable(String), `sample_id`"
            + " Nullable(String), `url` String, `display_name` Nullable(String), `metadata`"
            + " Nullable(String)) ENGINE = MergeTree ORDER BY (cancer_study_id, resource_id,"
            + " patient_id, sample_id, internal_id) SETTINGS allow_nullable_key = 1");
    query(
        "CREATE TABLE resource_compact_derived (`internal_id` Int64) ENGINE = MergeTree ORDER BY internal_id");
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
    assertThat(query("EXISTS TABLE resource_compact_derived")).isEqualTo("0");
    assertThat(
            query(
                "SELECT count() FROM system.mutations WHERE database = currentDatabase() AND"
                    + " NOT is_done"))
        .isEqualTo("0");

    // The section is idempotent: run again over a migrated table, it changes nothing.
    ExecResult second = migrate();
    assertThat(second.getExitCode()).as(second.getStderr()).isZero();
    assertThat(query(parity)).isEqualTo("0\t5006\t5012");

    client("-q", "DROP DATABASE " + DB);
  }
}
