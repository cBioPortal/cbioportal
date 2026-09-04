package org.cbioportal.domain.resource.usecase;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.cbioportal.domain.resource.ResourceColumnInfo;
import org.cbioportal.domain.resource.ResourceTableCounts;
import org.cbioportal.domain.resource.ResourceTableMetadataView;
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
      return new ResourceTableResult(
          List.of(), List.of(), List.of(), 0L, 0L, 0L, Map.of(), Map.of(), Map.of());
    }

    List<ResourceTableRow> rows = resourceDataRepository.getResourceTableRows(query);
    ResourceTableCounts counts = resourceDataRepository.getResourceTableCounts(query);
    ResourceTableMetadataView metadata = resourceDataRepository.getResourceTableMetadata(query);

    List<ResourceColumnInfo> columns = new ArrayList<>(builtinColumns());
    columns.addAll(metadata.columns());
    return new ResourceTableResult(
        List.of(),
        List.copyOf(columns),
        rows,
        counts.rowCount(),
        counts.patientCount(),
        counts.sampleCount(),
        metadata.facets(),
        metadata.facetRanges(),
        counts.distinctValueCounts());
  }

  private static List<ResourceColumnInfo> builtinColumns() {
    return List.of(
        new ResourceColumnInfo(
            "patientId",
            "Patient ID",
            ResourceColumnInfo.SOURCE_BUILTIN,
            "string",
            true,
            true,
            true,
            null),
        new ResourceColumnInfo(
            "sampleId",
            "Sample ID",
            ResourceColumnInfo.SOURCE_BUILTIN,
            "string",
            true,
            true,
            true,
            null),
        new ResourceColumnInfo(
            "url", "Link", ResourceColumnInfo.SOURCE_BUILTIN, "link", false, false, true, null),
        new ResourceColumnInfo(
            "displayName",
            "Display Name",
            ResourceColumnInfo.SOURCE_BUILTIN,
            "string",
            true,
            true,
            true,
            null),
        new ResourceColumnInfo(
            "type", "Type", ResourceColumnInfo.SOURCE_BUILTIN, "string", true, true, true, null),
        new ResourceColumnInfo(
            "priority",
            "Priority",
            ResourceColumnInfo.SOURCE_BUILTIN,
            "number",
            false,
            true,
            false,
            null));
  }
}
