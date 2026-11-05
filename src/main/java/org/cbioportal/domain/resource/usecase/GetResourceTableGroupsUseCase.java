package org.cbioportal.domain.resource.usecase;

import java.util.Set;
import org.cbioportal.domain.resource.InvalidResourceTableQueryException;
import org.cbioportal.domain.resource.ResourceGroupBy;
import org.cbioportal.domain.resource.ResourceGroupLocator;
import org.cbioportal.domain.resource.ResourceTableGroupPage;
import org.cbioportal.domain.resource.ResourceTableQuery;
import org.cbioportal.domain.resource.repository.ResourceDataRepository;
import org.cbioportal.domain.studyview.StudyViewService;
import org.springframework.stereotype.Service;

/**
 * Serves the resource table one patient or sample at a time: each group carries its row count and,
 * for the requested columns, how many of its rows hold each value. Filters and search apply to the
 * rows before they are grouped, so a group whose rows are all filtered out is not returned.
 */
@Service
public class GetResourceTableGroupsUseCase {

  /** A page of groups is a navigator's page, not an export. */
  public static final int MAX_PAGE_SIZE = 1000;

  /** Each counted column adds an aggregation per group. */
  public static final int MAX_GROUP_COUNTS = 10;

  private static final Set<String> GROUP_SORTS = Set.of("patientId", "studyId", "rowCount");

  private final ResourceDataRepository resourceDataRepository;
  private final StudyViewService studyViewService;

  public GetResourceTableGroupsUseCase(
      ResourceDataRepository resourceDataRepository, StudyViewService studyViewService) {
    this.resourceDataRepository = resourceDataRepository;
    this.studyViewService = studyViewService;
  }

  public ResourceTableGroupPage execute(ResourceTableQuery query) {
    validate(query);
    if (!GetResourceTableDataUseCase.isAnswerable(query)) {
      return ResourceTableGroupPage.empty();
    }
    return resourceDataRepository.getResourceTableGroups(
        query, ResourceQueryContexts.resolve(query, resourceDataRepository, studyViewService));
  }

  /** Rejects a request the groups endpoint cannot answer as asked, before anything is read. */
  static void validate(ResourceTableQuery query) {
    if (query == null || query.groupBy() == null) {
      throw new InvalidResourceTableQueryException("groupBy is required: 'patient' or 'sample'");
    }
    ResourceGroupBy groupBy = query.groupByMode();
    if (query.pageSize() < 1 || query.pageSize() > MAX_PAGE_SIZE) {
      throw new InvalidResourceTableQueryException(
          "pageSize must be between 1 and " + MAX_PAGE_SIZE + ", not " + query.pageSize());
    }
    if (query.pageNumber() < 0) {
      throw new InvalidResourceTableQueryException(
          "pageNumber must not be negative, not " + query.pageNumber());
    }
    if (query.offset() > Integer.MAX_VALUE) {
      throw new InvalidResourceTableQueryException(
          "pageNumber * pageSize must not exceed " + Integer.MAX_VALUE);
    }
    if (query.groupCountColumns().size() > MAX_GROUP_COUNTS) {
      throw new InvalidResourceTableQueryException(
          "groupCounts may name at most " + MAX_GROUP_COUNTS + " columns");
    }
    String sortBy = query.sortBy();
    if (sortBy != null
        && !GROUP_SORTS.contains(sortBy)
        && !(groupBy == ResourceGroupBy.SAMPLE && "sampleId".equals(sortBy))) {
      throw new InvalidResourceTableQueryException(
          "Groups sort by patientId, studyId or rowCount"
              + (groupBy == ResourceGroupBy.SAMPLE ? " or sampleId" : "")
              + ", not '"
              + sortBy
              + "'");
    }
    ResourceGroupLocator locate = query.locate();
    if (locate != null) {
      String key = query.locateKey();
      if (locate.studyId() == null || locate.studyId().isBlank() || key == null || key.isBlank()) {
        throw new InvalidResourceTableQueryException(
            "locate needs a studyId and a "
                + (groupBy == ResourceGroupBy.SAMPLE ? "sampleId" : "patientId"));
      }
    }
  }
}
