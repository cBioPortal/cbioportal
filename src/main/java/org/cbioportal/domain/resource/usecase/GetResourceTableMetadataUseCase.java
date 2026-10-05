package org.cbioportal.domain.resource.usecase;

import java.util.ArrayList;
import java.util.List;
import org.cbioportal.domain.resource.ResourceColumnInfo;
import org.cbioportal.domain.resource.ResourceTableCounts;
import org.cbioportal.domain.resource.ResourceTableMetadataResult;
import org.cbioportal.domain.resource.ResourceTableMetadataView;
import org.cbioportal.domain.resource.ResourceTableQuery;
import org.cbioportal.domain.resource.repository.ResourceDataRepository;
import org.springframework.stereotype.Service;

/** Serves the page-invariant half of the resource table; see ResourceTableMetadataResult. */
@Service
public class GetResourceTableMetadataUseCase {

  private final ResourceDataRepository resourceDataRepository;

  public GetResourceTableMetadataUseCase(ResourceDataRepository resourceDataRepository) {
    this.resourceDataRepository = resourceDataRepository;
  }

  public ResourceTableMetadataResult execute(ResourceTableQuery query) {
    if (!GetResourceTableDataUseCase.isAnswerable(query)) {
      return ResourceTableMetadataResult.empty();
    }

    ResourceTableCounts counts = resourceDataRepository.getResourceTableCounts(query);
    ResourceTableMetadataView metadata = resourceDataRepository.getResourceTableMetadata(query);

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
