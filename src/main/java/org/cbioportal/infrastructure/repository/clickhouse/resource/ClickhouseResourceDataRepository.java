package org.cbioportal.infrastructure.repository.clickhouse.resource;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.cbioportal.domain.resource.ResourceFacetOption;
import org.cbioportal.domain.resource.ResourceMetadataKeyStats;
import org.cbioportal.domain.resource.ResourceMetadataSchema;
import org.cbioportal.domain.resource.ResourceNumericRange;
import org.cbioportal.domain.resource.ResourceTableQuery;
import org.cbioportal.domain.resource.ResourceTableRow;
import org.cbioportal.domain.resource.ResourceTableTab;
import org.cbioportal.domain.resource.ResourceTabsRequest;
import org.cbioportal.domain.resource.repository.ResourceDataRepository;
import org.springframework.stereotype.Repository;

@Repository
public class ClickhouseResourceDataRepository implements ResourceDataRepository {
  // Only non-ID builtin columns that benefit from categorical filtering
  private static final Map<String, String> FACET_COLUMNS = Map.of("type", "rdata.TYPE");

  private final ClickhouseResourceDataMapper mapper;

  public ClickhouseResourceDataRepository(ClickhouseResourceDataMapper mapper) {
    this.mapper = mapper;
  }

  @Override
  public List<ResourceTableTab> getResourceTableTabs(ResourceTabsRequest request) {
    return mapper.getResourceTableTabs(request);
  }

  @Override
  public List<ResourceTableRow> getResourceTableRows(ResourceTableQuery query) {
    return mapper.getResourceTableRows(query);
  }

  @Override
  public Map<String, List<ResourceFacetOption>> getResourceTableFacets(ResourceTableQuery query) {
    Map<String, List<ResourceFacetOption>> facets = new LinkedHashMap<>();

    // Facet options (both which metadata columns exist, and each column's distinct values) are
    // always computed against the query with ALL column-level filters removed (only
    // resourceId/study/patient/sample/search scoping kept). This keeps every filter dropdown
    // showing its full, stable option set regardless of what's currently selected in ANY column
    // (itself or another) — e.g. deselecting every option in one column (a "1 = 0" filter that
    // zeroes out all matching rows) must not make that column's own options, or any other
    // column's options/keys, disappear.
    ResourceTableQuery queryWithoutColumnFilters = withoutColumnFilters(query);

    // Builtin columns (only 'type' — patientId/sampleId have too many values)
    for (Map.Entry<String, String> entry : FACET_COLUMNS.entrySet()) {
      List<ResourceFacetOption> values =
          mapper.getResourceTableFacetValues(queryWithoutColumnFilters, entry.getValue());
      if (values != null && !values.isEmpty()) {
        facets.put(entry.getKey(), values);
      }
    }

    // Dynamic metadata columns classified as categorical only — numeric columns are exposed via
    // getResourceTableFacetRanges() instead of an enumerated value list (which would be huge/
    // unhelpful for a continuous measurement column).
    for (Map.Entry<String, ResourceMetadataKeyStats> entry :
        classifyMetadataKeys(queryWithoutColumnFilters).entrySet()) {
      if (isNumericColumn(entry.getValue(), queryWithoutColumnFilters, entry.getKey())) {
        continue;
      }
      List<ResourceFacetOption> values =
          mapper.getResourceTableMetadataFacetValues(queryWithoutColumnFilters, entry.getKey());
      if (values != null && !values.isEmpty()) {
        facets.put("metadata:" + entry.getKey(), values);
      }
    }

    return facets;
  }

  @Override
  public Map<String, ResourceNumericRange> getResourceTableFacetRanges(ResourceTableQuery query) {
    ResourceTableQuery queryWithoutColumnFilters = withoutColumnFilters(query);
    Map<String, ResourceNumericRange> facetRanges = new LinkedHashMap<>();

    for (Map.Entry<String, ResourceMetadataKeyStats> entry :
        classifyMetadataKeys(queryWithoutColumnFilters).entrySet()) {
      ResourceMetadataKeyStats stats = entry.getValue();
      if (isNumericColumn(stats, queryWithoutColumnFilters, entry.getKey())) {
        facetRanges.put(
            "metadata:" + entry.getKey(),
            new ResourceNumericRange(stats.minValue(), stats.maxValue()));
      }
    }

    return facetRanges;
  }

  /**
   * Returns per-key stats for every metadata key discovered in the current tab, keyed by the raw
   * metadata key (not prefixed with "metadata:").
   */
  private Map<String, ResourceMetadataKeyStats> classifyMetadataKeys(ResourceTableQuery query) {
    List<ResourceMetadataKeyStats> stats = mapper.getResourceTableMetadataKeyStats(query);
    Map<String, ResourceMetadataKeyStats> byKey = new LinkedHashMap<>();
    if (stats != null) {
      for (ResourceMetadataKeyStats stat : stats) {
        byKey.put(stat.key(), stat);
      }
    }
    return byKey;
  }

  /**
   * Decides whether a metadata key should be treated as numeric, combining an optional {@code
   * resource_definition.custom_metadata} schema override with auto-detection:
   *
   * <ul>
   *   <li>Schema declares "string" -> always categorical (explicit override wins).
   *   <li>Schema declares "number" -> numeric, but only if at least one value actually parsed (a
   *       usable min/max range exists); otherwise falls back to categorical since a numeric filter
   *       with no range would be useless.
   *   <li>No schema entry for this key -> pure auto-detection (numeric only if every non-blank
   *       value parsed as a number).
   * </ul>
   */
  private boolean isNumericColumn(
      ResourceMetadataKeyStats stats, ResourceTableQuery query, String key) {
    String declaredType = getDeclaredMetadataTypes(query).get(key);
    if ("string".equals(declaredType)) {
      return false;
    }
    if ("number".equals(declaredType)) {
      return stats.hasUsableNumericRange();
    }
    return stats.isAutoDetectedNumeric();
  }

  private Map<String, String> getDeclaredMetadataTypes(ResourceTableQuery query) {
    String customMetadata = mapper.getResourceDefinitionCustomMetadata(query);
    return ResourceMetadataSchema.parseDeclaredTypes(customMetadata);
  }

  /** Returns a copy of the query with all column-level filters removed. */
  private ResourceTableQuery withoutColumnFilters(ResourceTableQuery query) {
    if (query.filters() == null || query.filters().isEmpty()) {
      return query;
    }
    return new ResourceTableQuery(
        query.studyIds(),
        query.resourceId(),
        query.patientIds(),
        query.sampleIds(),
        query.search(),
        query.pageNumber(),
        query.pageSize(),
        query.sortBy(),
        query.direction(),
        List.of());
  }

  @Override
  public long getResourceTableRowCount(ResourceTableQuery query) {
    return mapper.getResourceTableRowCount(query);
  }

  @Override
  public long getResourceTablePatientCount(ResourceTableQuery query) {
    return mapper.getResourceTablePatientCount(query);
  }

  @Override
  public long getResourceTableSampleCount(ResourceTableQuery query) {
    return mapper.getResourceTableSampleCount(query);
  }
}
