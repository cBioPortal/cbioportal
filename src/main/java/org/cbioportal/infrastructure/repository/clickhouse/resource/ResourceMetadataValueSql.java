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
 * backquoted, which makes any text a single identifier naming that key's path. Every value is
 * stored as a String with a ':' prefix, so that ClickHouse infers no type from it; {@code
 * substring} drops it again. See {@code metadata_json} in schema.sql.
 *
 * <p>The path is read as its typed subcolumn, {@code metadata_json.`key`.:String}, which reads the
 * String values directly rather than casting the path's Dynamic column. Since every stored value is
 * a String the two give the same result, NULL included for a missing path, whether the path is
 * stored as its own subcolumn or in the shared data past {@code max_dynamic_paths}.
 *
 * <p>Some keys are read from the raw document instead, with {@code JSONExtractString}:
 *
 * <ul>
 *   <li>the empty key, which has no identifier;
 *   <li>keys containing {@code .:} or {@code .^}, which the subcolumn syntax reads as a type hint
 *       ({@code json.a.:String}) or a sub-object ({@code json.^a}). Such a key followed by a
 *       backquoted part, like {@code a.:`b`}, names no subcolumn and fails with UNKNOWN_IDENTIFIER.
 * </ul>
 *
 * <p>The key is escaped for the backquoted identifier or the quoted string it is spliced into: a
 * backslash and the quote character get a backslash, and {@code #} is written {@code \x23}. MyBatis
 * parses {@code #{...}} placeholders in the SQL after {@code ${...}} has been substituted, so a key
 * containing {@code #{search}} would otherwise become a bound parameter. {@code ${...}} in a key
 * needs no escape, since the substituted text is not parsed for it again.
 *
 * <p>The mapper XML calls {@link #of(String)} through OGNL, as {@code
 * ${@org.cbioportal.infrastructure.repository.clickhouse.resource.ResourceMetadataValueSql@of(key)}}.
 */
public final class ResourceMetadataValueSql {

  private ResourceMetadataValueSql() {}

  /** The key's value, read from its subcolumn of {@code metadata_json} where one names it. */
  public static String of(String key) {
    if (key == null || key.isEmpty() || key.contains(".:") || key.contains(".^")) {
      return raw(key);
    }
    return "substring(ifNull(rdata.metadata_json.`" + escape(key, '`') + "`.:String, ''), 2)";
  }

  /**
   * The key's value, read from the raw {@code metadata} document. For a statement that reads the
   * raw documents anyway, as a search does, this is cheaper than also reading the subcolumns.
   */
  public static String raw(String key) {
    return "JSONExtractString(ifNull(rdata.metadata, '{}'), '"
        + escape(key == null ? "" : key, '\'')
        + "')";
  }

  /** Escapes text for a ClickHouse identifier or string literal delimited by {@code quote}. */
  private static String escape(String text, char quote) {
    StringBuilder escaped = new StringBuilder(text.length() + 8);
    for (int i = 0; i < text.length(); i++) {
      char c = text.charAt(i);
      if (c == '#') {
        escaped.append("\\x23");
      } else {
        if (c == '\\' || c == quote) {
          escaped.append('\\');
        }
        escaped.append(c);
      }
    }
    return escaped.toString();
  }
}
