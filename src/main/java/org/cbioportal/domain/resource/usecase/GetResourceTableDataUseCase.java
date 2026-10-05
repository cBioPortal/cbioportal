package org.cbioportal.domain.resource.usecase;

import java.util.List;
import org.cbioportal.domain.resource.ResourceColumnInfo;
import org.cbioportal.domain.resource.ResourceTableQuery;
import org.cbioportal.domain.resource.ResourceTableRow;
import org.cbioportal.domain.resource.repository.ResourceDataRepository;
import org.springframework.stereotype.Service;

@Service
public class GetResourceTableDataUseCase {
  private final ResourceDataRepository resourceDataRepository;

  public GetResourceTableDataUseCase(ResourceDataRepository resourceDataRepository) {
    this.resourceDataRepository = resourceDataRepository;
  }

  /**
   * Returns just the requested page. Columns, facets and counts come from {@link
   * GetResourceTableMetadataUseCase}: they do not change as the user pages, and recomputing them
   * per page was most of the cost of a large resource's response.
   */
  public List<ResourceTableRow> execute(ResourceTableQuery query) {
    if (!isAnswerable(query)) {
      return List.of();
    }
    return resourceDataRepository.getResourceTableRows(query);
  }

  /** A query naming no study or no resource cannot be answered, and is not an error. */
  static boolean isAnswerable(ResourceTableQuery query) {
    return query != null
        && query.studyIds() != null
        && !query.studyIds().isEmpty()
        && query.resourceId() != null
        && !query.resourceId().isBlank();
  }

  static List<ResourceColumnInfo> builtinColumns() {
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
            "type", "Type", ResourceColumnInfo.SOURCE_BUILTIN, "string", true, true, true, null));
  }
}
