package org.cbioportal.domain.resource.usecase;

import java.util.ArrayList;
import java.util.List;
import org.cbioportal.domain.resource.ResourceColumnInfo;
import org.cbioportal.domain.resource.ResourceQueryContext;
import org.cbioportal.domain.resource.ResourceTableCounts;
import org.cbioportal.domain.resource.ResourceTableMetadataResult;
import org.cbioportal.domain.resource.ResourceTableMetadataView;
import org.cbioportal.domain.resource.ResourceTableQuery;
import org.cbioportal.domain.resource.repository.ResourceDataRepository;
import org.cbioportal.domain.studyview.StudyViewService;
import org.springframework.stereotype.Service;

/** Serves the page-invariant half of the resource table; see ResourceTableMetadataResult. */
@Service
public class GetResourceTableMetadataUseCase {

  private final ResourceDataRepository resourceDataRepository;
  private final StudyViewService studyViewService;

  public GetResourceTableMetadataUseCase(
      ResourceDataRepository resourceDataRepository, StudyViewService studyViewService) {
    this.resourceDataRepository = resourceDataRepository;
    this.studyViewService = studyViewService;
  }

  /**
   * With {@code groupBy} set, only the facets change: each facet value counts the groups carrying
   * it, i.e. how many patients (or samples) have a row with that value, over the rows that belong
   * to a group. The columns, the numeric ranges and the row, patient and sample counts are the same
   * as for the query without {@code groupBy}. {@code groupCounts}, {@code locate} and paging do not
   * apply here.
   */
  public ResourceTableMetadataResult execute(ResourceTableQuery query) {
    if (query != null) {
      // Rejects a groupBy that is not a grouping mode before anything is read.
      query.groupByMode();
    }
    if (!GetResourceTableDataUseCase.isAnswerable(query)) {
      return ResourceTableMetadataResult.empty();
    }

    ResourceQueryContext context =
        ResourceQueryContexts.resolve(query, resourceDataRepository, studyViewService);
    ResourceTableCounts counts = resourceDataRepository.getResourceTableCounts(query, context);
    ResourceTableMetadataView metadata =
        resourceDataRepository.getResourceTableMetadata(query, context);

    List<ResourceColumnInfo> columns =
        new ArrayList<>(GetResourceTableDataUseCase.builtinColumns());
    columns.addAll(metadata.columns());

    return new ResourceTableMetadataResult(
        List.copyOf(columns),
        counts.rowCount(),
        counts.patientCount(),
        counts.sampleCount(),
        metadata.facets(),
        metadata.facetRanges(),
        counts.distinctValueCounts());
  }
}
