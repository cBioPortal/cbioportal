package org.cbioportal.domain.resource.usecase;

import java.util.List;
import java.util.Map;
import org.cbioportal.domain.resource.ResourceColumnInfo;
import org.cbioportal.domain.resource.ResourceFacetOption;
import org.cbioportal.domain.resource.ResourceTableQuery;
import org.cbioportal.domain.resource.ResourceTableResult;
import org.cbioportal.domain.resource.ResourceTableRow;
import org.cbioportal.domain.resource.repository.ResourceDataRepository;
import org.springframework.stereotype.Service;

@Service
public class GetResourceTableDataUseCase {
  private final ResourceDataRepository resourceDataRepository;

  public GetResourceTableDataUseCase(ResourceDataRepository resourceDataRepository) {
    this.resourceDataRepository = resourceDataRepository;
  }

  public ResourceTableResult execute(ResourceTableQuery query) {
    if (query == null
        || query.studyIds() == null
        || query.studyIds().isEmpty()
        || query.resourceId() == null
        || query.resourceId().isBlank()) {
      return new ResourceTableResult(List.of(), List.of(), List.of(), 0L, 0L, 0L, Map.of());
    }

    List<ResourceTableRow> rows = resourceDataRepository.getResourceTableRows(query);
    long totalRowCount = resourceDataRepository.getResourceTableRowCount(query);
    long patientCount = resourceDataRepository.getResourceTablePatientCount(query);
    long sampleCount = resourceDataRepository.getResourceTableSampleCount(query);
    Map<String, List<ResourceFacetOption>> facets =
        resourceDataRepository.getResourceTableFacets(query);
    return new ResourceTableResult(
        List.of(), defaultColumns(), rows, totalRowCount, patientCount, sampleCount, facets);
  }

  private static List<ResourceColumnInfo> defaultColumns() {
    return List.of(
        new ResourceColumnInfo("patientId", "Patient ID", "builtin", "string", true, true, true),
        new ResourceColumnInfo("sampleId", "Sample ID", "builtin", "string", true, true, true),
        new ResourceColumnInfo("url", "Link", "builtin", "link", false, false, true),
        new ResourceColumnInfo(
            "displayName", "Display Name", "builtin", "string", true, true, true),
        new ResourceColumnInfo("type", "Type", "builtin", "string", true, true, true),
        new ResourceColumnInfo("priority", "Priority", "builtin", "number", false, true, false));
  }
}
