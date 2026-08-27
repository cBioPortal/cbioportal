package org.cbioportal.infrastructure.repository.clickhouse.resource;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.cbioportal.domain.resource.ResourceFacetOption;
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

    // Dynamic metadata columns — discover keys then get distinct values for each.
    List<String> metadataKeys = mapper.getResourceTableMetadataKeys(queryWithoutColumnFilters);
    if (metadataKeys != null) {
      for (String key : metadataKeys) {
        List<ResourceFacetOption> values =
            mapper.getResourceTableMetadataFacetValues(queryWithoutColumnFilters, key);
        if (values != null && !values.isEmpty()) {
          facets.put("metadata:" + key, values);
        }
      }
    }

    return facets;
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
