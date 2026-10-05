package org.cbioportal.domain.resource;

import java.util.List;
import java.util.Map;

/**
 * Everything about a resource table that does not depend on which page is being viewed: the
 * columns, their filter options, and the counts over the whole filtered set.
 *
 * <p>This is split from the rows because it is expensive and page-invariant. Computing it per page
 * meant key discovery and one facet aggregation per key ran again on every page change, which on a
 * 1.17M-row resource was most of a 14s response. A client fetches this once per (study, resource,
 * cohort, search, filters) and reuses it while the user pages.
 */
public record ResourceTableMetadataResult(
    List<ResourceColumnInfo> columns,
    long totalRowCount,
    long filteredPatientCount,
    long filteredSampleCount,
    Map<String, List<ResourceFacetOption>> facets,
    Map<String, ResourceNumericRange> facetRanges,
    Map<String, Long> distinctValueCounts) {

  public static ResourceTableMetadataResult empty() {
    return new ResourceTableMetadataResult(List.of(), 0L, 0L, 0L, Map.of(), Map.of(), Map.of());
  }
}
