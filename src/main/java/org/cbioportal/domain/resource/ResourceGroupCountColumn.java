package org.cbioportal.domain.resource;

/**
 * One column a grouped query counts values of within each group: a metadata key ({@code
 * metadata:<key>}) or the builtin {@code resourceId}, which tells apart the resources a combined
 * query reads.
 *
 * <p>Accessors are record-style because MyBatis reads them off a {@code <foreach>} item; see {@link
 * ResourceColumnFilter#metadataKey()}.
 *
 * @param columnId the column id as requested, which also keys the counts in the response
 * @param metadataKey the metadata key for a metadata column, or null for {@code resourceId}
 */
public record ResourceGroupCountColumn(String columnId, String metadataKey) {

  public static final String RESOURCE_ID = "resourceId";

  public static ResourceGroupCountColumn parse(String columnId) {
    if (RESOURCE_ID.equals(columnId)) {
      return new ResourceGroupCountColumn(columnId, null);
    }
    if (columnId != null
        && columnId.startsWith(ResourceColumnInfo.METADATA_COLUMN_PREFIX)
        && columnId.length() > ResourceColumnInfo.METADATA_COLUMN_PREFIX.length()) {
      return new ResourceGroupCountColumn(
          columnId, columnId.substring(ResourceColumnInfo.METADATA_COLUMN_PREFIX.length()));
    }
    throw new InvalidResourceTableQueryException(
        "groupCounts entries must be 'resourceId' or 'metadata:<key>', not '" + columnId + "'");
  }
}
