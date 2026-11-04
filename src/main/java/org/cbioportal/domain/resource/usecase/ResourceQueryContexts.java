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

  /**
   * Resolves the study-view cohort, if the query has one, and the resource scope, if the query
   * needs it: grouped queries do, to choose their table, and so does any query naming {@code
   * resourceIds}, whose ids are validated against it.
   *
   * @throws InvalidResourceTableQueryException when a scoped query names a resource that none of
   *     its studies defines or holds rows of
   */
  static ResourceQueryContext resolve(
      ResourceTableQuery query,
      ResourceDataRepository repository,
      StudyViewService studyViewService) {
    StudyViewFilterContext cohort =
        query.hasStudyViewFilter()
            ? studyViewService.buildStudyViewFilterContext(query.studyViewFilter())
            : null;
    ResourceScope scope = null;
    if (query.grouped() || query.hasResourceIds()) {
      ResourceScope found = repository.getResourceScope(query);
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
