package org.cbioportal.domain.resource;

import java.util.Map;

/**
 * The three counts a resource-table response reports, plus how many distinct values a few builtin
 * columns carry, all gathered in a single pass over the filtered rows.
 *
 * <p>A builtin column holding one value across every row conveys nothing — in a single-resource tab
 * "Resource Type" is always the resource's own name, and "Scope" is usually one entity type — so
 * the response hides it by default rather than spending a column on it.
 */
public record ResourceTableCounts(
    long rowCount,
    long patientCount,
    long sampleCount,
    long distinctResourceDisplayNames,
    long distinctScopes,
    long distinctDisplayNames) {

  public static ResourceTableCounts empty() {
    return new ResourceTableCounts(0L, 0L, 0L, 0L, 0L, 0L);
  }

  /**
   * Distinct value counts keyed by the backend field name the frontend maps its builtin columns
   * onto.
   */
  public Map<String, Long> distinctValueCounts() {
    return Map.of(
        "resourceDisplayName", distinctResourceDisplayNames,
        "type", distinctScopes,
        "displayName", distinctDisplayNames);
  }
}
