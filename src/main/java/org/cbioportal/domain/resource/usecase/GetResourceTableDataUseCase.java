package org.cbioportal.domain.resource.usecase;

import java.util.List;
import java.util.Map;
import org.cbioportal.domain.resource.ResourceColumnInfo;
import org.cbioportal.domain.resource.ResourceTableQuery;
import org.cbioportal.domain.resource.ResourceTableRow;
import org.cbioportal.domain.resource.ResourceUrlTemplate;
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
   * <p>A row without a URL of its own is given its study's {@code url_template} for the resource,
   * expanded against {@code baseUrl}; see {@link ResourceUrlTemplate}.
   *
   * @param baseUrl the portal's base URL for the current request, substituted for {@code
   *     ${base_url}}
   */
  public List<ResourceTableRow> execute(ResourceTableQuery query, String baseUrl) {
    if (!isAnswerable(query)) {
      return List.of();
    }
    List<ResourceTableRow> rows = resourceDataRepository.getResourceTableRows(query);
    if (rows.stream().allMatch(row -> row.url() != null && !row.url().isBlank())) {
      return rows;
    }
    Map<String, String> templates = resourceDataRepository.getResourceUrlTemplates(query);
    if (templates.isEmpty()) {
      return rows;
    }
    return rows.stream().map(row -> withResolvedUrl(row, templates, baseUrl)).toList();
  }

  private static ResourceTableRow withResolvedUrl(
      ResourceTableRow row, Map<String, String> templates, String baseUrl) {
    String template = templates.get(row.studyId());
    if (template == null || (row.url() != null && !row.url().isBlank())) {
      return row;
    }
    String url =
        ResourceUrlTemplate.resolve(
            row.url(),
            template,
            baseUrl,
            new ResourceUrlTemplate.Values(
                row.studyId(), row.resourceId(), row.patientId(), row.sampleId(), row.metadata()));
    return new ResourceTableRow(
        row.studyId(),
        row.resourceId(),
        row.resourceDisplayName(),
        row.resourceType(),
        row.patientId(),
        row.sampleId(),
        url,
        row.displayName(),
        row.metadata());
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
