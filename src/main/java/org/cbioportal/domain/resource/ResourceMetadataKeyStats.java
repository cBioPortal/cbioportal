package org.cbioportal.domain.resource;

/**
 * Per-metadata-key stats used to decide whether a dynamic metadata column should be treated as
 * numeric (dual-handle range slider filter) or categorical (checkbox filter). The slider's bounds
 * come from {@link ResourceMetadataRange}, not from here: these are derived from a bounded sample
 * and would understate the true range. See {@link
 * org.cbioportal.infrastructure.repository.clickhouse.resource.ClickhouseResourceDataRepository}
 * for how this is combined with an optional {@code resource_definition.custom_metadata} schema
 * override.
 */
public record ResourceMetadataKeyStats(String key, long nonBlankCount, long numericCount) {

  /** True when every non-blank value for this key, across the current rows, parses as a number. */
  public boolean isAutoDetectedNumeric() {
    return nonBlankCount > 0 && numericCount == nonBlankCount;
  }
}
