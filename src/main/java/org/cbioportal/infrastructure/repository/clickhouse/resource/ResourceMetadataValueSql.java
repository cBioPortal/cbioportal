package org.cbioportal.infrastructure.repository.clickhouse.resource;

/**
 * The SQL reading one metadata key's value from a resource row ({@code rdata}), as {@code
 * JSONExtractString(metadata, key)} gives it: a string unquoted, any other value as its JSON text,
 * '' for null or a missing key.
 *
 * <p>It reads the key's subcolumn of {@code metadata_json}, which {@code resource} materializes
 * from {@code metadata} on insert, rather than parsing each row's whole document. The subcolumn has
 * to be named as an identifier: {@code getSubcolumn(metadata_json, 'key')} gives the same value but
 * reads the whole column. So the key, which comes from the request, is spliced into the SQL here,
 * backquoted with its backslashes and backquotes escaped, which makes any text a single identifier
 * naming that key's path. Every value is stored with a ':' prefix, so that ClickHouse infers no
 * type from it; {@code substring} drops it again. See {@code metadata_json} in schema.sql.
 *
 * <p>ClickHouse has no identifier for the empty key, so that one key is read from the raw document.
 *
 * <p>The mapper XML calls {@link #of(String)} through OGNL, as {@code
 * ${@org.cbioportal.infrastructure.repository.clickhouse.resource.ResourceMetadataValueSql@of(key)}}.
 */
public final class ResourceMetadataValueSql {

  private ResourceMetadataValueSql() {}

  public static String of(String key) {
    if (key == null || key.isEmpty()) {
      return "JSONExtractString(ifNull(rdata.metadata, '{}'), '')";
    }
    return "substring(ifNull(CAST(rdata.metadata_json.`"
        + key.replace("\\", "\\\\").replace("`", "\\`")
        + "`, 'Nullable(String)'), ''), 2)";
  }
}
