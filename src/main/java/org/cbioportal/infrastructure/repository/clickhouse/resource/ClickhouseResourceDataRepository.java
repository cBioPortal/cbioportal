package org.cbioportal.infrastructure.repository.clickhouse.resource;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.cbioportal.domain.resource.ResourceColumnFilter;
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

    // Builtin columns (only 'type' — patientId/sampleId have too many values)
    for (Map.Entry<String, String> entry : FACET_COLUMNS.entrySet()) {
      ResourceTableQuery queryWithoutOwnFilter = withoutFilterOnColumn(query, entry.getKey());
      List<ResourceFacetOption> values =
          mapper.getResourceTableFacetValues(queryWithoutOwnFilter, entry.getValue());
      if (values != null && !values.isEmpty()) {
        facets.put(entry.getKey(), values);
      }
    }

    // Dynamic metadata columns — discover keys then get distinct values for each.
    // Key discovery ignores ALL column-level filters (but keeps resourceId/study/patient/
    // sample/search scoping) so that the set of available metadata columns stays stable
    // regardless of the current filter selection — e.g. deselecting every option in one
    // column's filter (which zeroes out matching rows) must not make other metadata
    // columns disappear from the table / "Add columns" list.
    ResourceTableQuery queryWithoutColumnFilters = withoutColumnFilters(query);
    List<String> metadataKeys = mapper.getResourceTableMetadataKeys(queryWithoutColumnFilters);
    if (metadataKeys != null) {
      for (String key : metadataKeys) {
        ResourceTableQuery queryWithoutOwnFilter = withoutFilterOnColumn(query, "metadata:" + key);
        List<ResourceFacetOption> values =
            mapper.getResourceTableMetadataFacetValues(queryWithoutOwnFilter, key);
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

  /**
   * Returns a copy of the query with any filter on the given column removed, so that a column's own
   * facet values are computed independently of its own active selection (otherwise deselecting an
   * option would make it disappear from the facet list entirely, instead of remaining available to
   * re-select).
   */
  private ResourceTableQuery withoutFilterOnColumn(ResourceTableQuery query, String columnId) {
    if (query.filters() == null || query.filters().isEmpty()) {
      return query;
    }
    List<ResourceColumnFilter> filtersWithoutColumn =
        query.filters().stream()
            .filter(filter -> filter == null || !columnId.equals(filter.columnId()))
            .collect(Collectors.toList());
    if (filtersWithoutColumn.size() == query.filters().size()) {
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
        filtersWithoutColumn);
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
