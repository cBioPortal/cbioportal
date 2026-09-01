package org.cbioportal.domain.resource;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;

public record ResourceColumnFilter(String columnId, String operator, List<String> values) {

  /** Prefix marking a filter that targets a dynamic key inside {@code resource_data.METADATA}. */
  public static final String METADATA_COLUMN_PREFIX = "metadata:";

  /** Whether this filter targets a dynamic metadata key rather than a base column. */
  @JsonIgnore
  public boolean metadataColumn() {
    return columnId != null && columnId.startsWith(METADATA_COLUMN_PREFIX);
  }

  /**
   * The metadata JSON key this filter targets, or {@code null} for a base (non-metadata) column.
   *
   * <p>MyBatis reads this off the {@code <foreach>} item as {@code #{filter.metadataKey}}. The
   * accessor is deliberately record-style (not {@code getMetadataKey()}): for records, MyBatis'
   * {@code Reflector} registers properties under the raw method name, so a bean-style getter would
   * be exposed as the property {@code getMetadataKey} instead.
   *
   * <p>This must NOT be derived with a {@code <bind>} inside the filters loop — see the comment on
   * {@code ApplyStringFilterOnMetadata} in {@code ResourceDataMapper.xml}.
   */
  @JsonIgnore
  public String metadataKey() {
    return metadataColumn() ? columnId.substring(METADATA_COLUMN_PREFIX.length()) : null;
  }
}
