package org.cbioportal.domain.resource.usecase;

import java.util.List;
import java.util.Set;
import org.cbioportal.domain.resource.InvalidResourceTableQueryException;
import org.cbioportal.domain.resource.ResourceQueryContext;
import org.cbioportal.domain.resource.ResourceScope;
import org.cbioportal.domain.resource.ResourceTableQuery;
import org.cbioportal.domain.resource.repository.ResourceDataRepository;
import org.cbioportal.domain.studyview.StudyViewFilterContext;
import org.cbioportal.domain.studyview.StudyViewService;

/** Resolves a resource-table query into the {@link ResourceQueryContext} its reads take. */
final class ResourceQueryContexts {

  private ResourceQueryContexts() {}

  /** Each counted column adds an aggregation per group. */
  static final int MAX_GROUP_COUNTS = 10;

  /**
   * Rejects grouping parameters a request cannot use: {@code groupCounts} or {@code locate} without
   * {@code groupBy}, which no endpoint would read, a {@code groupCounts} entry that is not a
   * countable column, and more than {@link #MAX_GROUP_COUNTS} of them. Every endpoint taking the
   * query applies the same rule, so a body one endpoint accepts another does not silently ignore in
   * part.
   */
  static void validateGroupingParameters(ResourceTableQuery query) {
    if (query == null) {
      return;
    }
    boolean hasGroupCounts = query.groupCounts() != null && !query.groupCounts().isEmpty();
    if (!query.grouped() && (hasGroupCounts || query.locate() != null)) {
      throw new InvalidResourceTableQueryException(
          "groupCounts and locate apply only to a grouped query; set groupBy");
    }
    if (query.groupCountColumns().size() > MAX_GROUP_COUNTS) {
      throw new InvalidResourceTableQueryException(
          "groupCounts may name at most " + MAX_GROUP_COUNTS + " columns");
    }
  }

  /**
   * Resolves the study-view cohort, if the query has one, and the resource scope, if the query
   * needs it: grouped queries do, to choose their table, and so does any query naming {@code
   * resourceIds}, whose ids are validated against it.
   *
   * @param scansMetadata whether the request will parse the metadata of every row the query
   *     matches, as facets and metadata column filters do; see {@link
   *     ResourceDataRepository#getResourceScope}
   * @throws InvalidResourceTableQueryException when a scoped query names a resource that none of
   *     its studies defines or holds rows of
   */
  static ResourceQueryContext resolve(
      ResourceTableQuery query,
      ResourceDataRepository repository,
      StudyViewService studyViewService,
      boolean scansMetadata) {
    StudyViewFilterContext cohort =
        query.hasStudyViewFilter()
            ? studyViewService.buildStudyViewFilterContext(query.studyViewFilter())
            : null;
    ResourceScope scope = null;
    if (query.grouped() || query.hasResourceIds()) {
      ResourceScope found = repository.getResourceScope(query, scansMetadata);
      scope = found == null ? new ResourceScope(List.of()) : found;
      Set<String> known = scope.resourceIds();
      List<String> unknown =
          query.effectiveResourceIds().stream().filter(id -> !known.contains(id)).toList();
      if (!unknown.isEmpty()) {
        throw new InvalidResourceTableQueryException(
            "Unknown resource " + unknown + " in studies " + query.studyIds());
      }
    }
    return cohort == null && scope == null
        ? ResourceQueryContext.none()
        : new ResourceQueryContext(cohort, scope);
  }
}
