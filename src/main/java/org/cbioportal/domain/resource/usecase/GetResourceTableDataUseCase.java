package org.cbioportal.domain.resource.usecase;

import java.util.List;
import org.cbioportal.domain.resource.ResourceColumnInfo;
import org.cbioportal.domain.resource.ResourceTableQuery;
import org.cbioportal.domain.resource.ResourceTableRow;
import org.cbioportal.domain.resource.ResourceUrlTemplate;
import org.cbioportal.domain.resource.StoredResourceTableRow;
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
   *
   * <p>A row without a URL of its own is given its own resource definition's {@code url_template},
   * expanded against {@code baseUrl}; see {@link ResourceUrlTemplate}. Each row carries its
   * definition's template, so rows of different resources or studies each use their own.
   *
   * @param baseUrl the portal's base URL for the current request, substituted for {@code
   *     ${base_url}}
   */
  public List<ResourceTableRow> execute(ResourceTableQuery query, String baseUrl) {
    if (!isAnswerable(query)) {
      return List.of();
    }
    return resourceDataRepository.getResourceTableRows(query).stream()
        .map(row -> served(row, baseUrl))
        .toList();
  }

  /** The row as served: its own URL if it has one, otherwise its template expanded. */
  static ResourceTableRow served(StoredResourceTableRow row, String baseUrl) {
    if (ResourceUrlTemplate.hasUrl(row.url()) || row.urlTemplate() == null) {
      return row.toRow(row.url());
    }
    return row.toRow(
        ResourceUrlTemplate.resolve(
            row.url(),
            row.urlTemplate(),
            baseUrl,
            new ResourceUrlTemplate.Values(
                row.studyId(), row.resourceId(), row.patientId(), row.sampleId(), row.metadata())));
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
            null));
  }
}
