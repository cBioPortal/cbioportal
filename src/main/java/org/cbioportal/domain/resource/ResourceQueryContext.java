package org.cbioportal.domain.resource;

import org.cbioportal.domain.studyview.StudyViewFilterContext;

/**
 * What a resource-table request resolves to on the server before any rows are read.
 *
 * @param cohort the request's study-view filter, resolved the way the study-view endpoints resolve
 *     it; null when the cohort is given as identifier lists
 * @param scope the requested resources in the requested studies; null when the request reads a
 *     single resource without grouping, which needs neither validation nor the compact table
 */
public record ResourceQueryContext(StudyViewFilterContext cohort, ResourceScope scope) {

  private static final ResourceQueryContext NONE = new ResourceQueryContext(null, null);

  public static ResourceQueryContext none() {
    return NONE;
  }

  /**
   * Whether a query is read from the compact metadata table: only grouped queries, which never
   * return metadata, only without a search, which matches text anywhere in a row's full metadata,
   * and only where the table holds everything they read.
   */
  public boolean readsCompactTable(ResourceTableQuery query) {
    return query.grouped()
        && !query.hasSearch()
        && scope != null
        && scope.compactCovers(query.referencedMetadataKeys());
  }
}
