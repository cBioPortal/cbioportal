# ClickHouse schema notes

Notes on schema columns whose contents or constraints aren't evident from `init/schema.sql`.
Migration steps live in `migrate/migrate_schema.sql`; deployment requirements in
[the ClickHouse setup guide](../../../../../docs/deployment/clickhouse/README.md).

## 3.2.0: `resource.metadata_json`

`metadata_json` holds `metadata` as a ClickHouse `JSON` column with one subcolumn per key, so a
query that filters, counts or facets on a few keys reads only those keys.

- It is `MATERIALIZED` from `metadata` on every insert. Importers don't write it, and it can't go
  out of date.
- Each value is the text `JSONExtractString(metadata, key)` returns:
  - a string is unescaped;
  - any other value keeps its JSON text;
  - `null` becomes `''`.
- Each stored value is prefixed with `:`, so ClickHouse infers no type from it. Without the prefix
  a date-like string such as `"2024-1-1"` would be reparsed as a `Date`. Readers strip the prefix
  with `substring(..., 2)`; see `ResourceMetadataValueSql`.
- A duplicate key keeps its first value, even when that value is `null`, matching
  `JSONExtractString`.
- Keys with a `null` or empty value are left out and read back as `''`.
- Invalid JSON gives an empty object.
- Queries name a key's subcolumn as an identifier (``metadata_json.`key`.:String``).
  `getSubcolumn()` would read the whole column. Keys that the subcolumn syntax can't express are
  read from `metadata` with `JSONExtractString` instead: the empty key, and keys containing `.:` or
  `.^`.
- On ClickHouse 25.3, an insert needs the `JSON` type enabled in the inserting user's profile
  (`enable_json_type = 1`, or a `compatibility` of 25.3 or later).
