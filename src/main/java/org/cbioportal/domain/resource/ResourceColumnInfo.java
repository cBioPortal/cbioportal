package org.cbioportal.domain.resource;

/**
 * Presentation info for one resource-table column.
 *
 * <p>{@code source} is {@code "builtin"} for the fixed columns and {@code "metadata"} for dynamic
 * columns discovered in {@code resource_data.METADATA}, whose {@code id} is {@code "metadata:"}
 * plus the metadata key. For metadata columns the values here are the result of merging
 * auto-detection with the optional {@code resource_definition.custom_metadata} contract.
 */
public record ResourceColumnInfo(
    String id,
    String label,
    String source,
    String dataType,
    boolean filterable,
    boolean sortable,
    boolean visibleByDefault,
    String description) {

  public static final String SOURCE_BUILTIN = "builtin";
  public static final String SOURCE_METADATA = "metadata";
  public static final String METADATA_COLUMN_PREFIX = "metadata:";
}
