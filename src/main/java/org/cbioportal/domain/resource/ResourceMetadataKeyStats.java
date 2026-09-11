package org.cbioportal.domain.resource;

/**
 * Per-metadata-key stats used to decide whether a dynamic metadata column should be treated as
 * numeric (dual-handle range slider filter) or categorical (checkbox filter). See {@link
 * org.cbioportal.infrastructure.repository.clickhouse.resource.ClickhouseResourceDataRepository}
 * for how this is combined with an optional {@code resource_definition.custom_metadata} schema
 * override.
 */
public record ResourceMetadataKeyStats(
    String key, long nonBlankCount, long numericCount, Double minValue, Double maxValue) {

  /** True when every non-blank value for this key, across the current rows, parses as a number. */
  public boolean isAutoDetectedNumeric() {
    return nonBlankCount > 0 && numericCount == nonBlankCount;
  }

  /** True when at least one value parsed as a number, so a usable min/max range exists. */
  public boolean hasUsableNumericRange() {
    return numericCount > 0 && minValue != null && maxValue != null;
  }
}
