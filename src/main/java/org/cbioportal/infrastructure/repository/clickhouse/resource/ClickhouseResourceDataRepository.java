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

    // Builtin columns (only 'type' — patientId/sampleId have too many values)
    for (Map.Entry<String, String> entry : FACET_COLUMNS.entrySet()) {
      List<ResourceFacetOption> values =
          mapper.getResourceTableFacetValues(query, entry.getValue());
      if (values != null && !values.isEmpty()) {
        facets.put(entry.getKey(), values);
      }
    }

    // Dynamic metadata columns — discover keys then get distinct values for each
    List<String> metadataKeys = mapper.getResourceTableMetadataKeys(query);
    if (metadataKeys != null) {
      for (String key : metadataKeys) {
        List<ResourceFacetOption> values = mapper.getResourceTableMetadataFacetValues(query, key);
        if (values != null && !values.isEmpty()) {
          facets.put("metadata:" + key, values);
        }
      }
    }

    return facets;
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
