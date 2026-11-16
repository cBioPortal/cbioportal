package org.cbioportal.domain.resource;

import java.util.List;
import java.util.Map;

/**
 * Everything the response needs about the dynamic metadata columns: which columns exist and how
 * they present, plus the filter options for the categorical ones and the ranges for the numeric
 * ones.
 *
 * <p>Returned as a unit because all three are derived from the same two lookups — the resource's
 * {@code custom_metadata} contract and the per-key stats. Asking for them separately meant
 * resolving both once per question.
 */
public record ResourceTableMetadataView(
    List<ResourceColumnInfo> columns,
    Map<String, List<ResourceFacetOption>> facets,
    Map<String, ResourceNumericRange> facetRanges) {

  public static ResourceTableMetadataView empty() {
    return new ResourceTableMetadataView(List.of(), Map.of(), Map.of());
  }
}
