package org.cbioportal.infrastructure.repository.clickhouse.resource;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.cbioportal.domain.resource.ResourceFacetOption;
import org.cbioportal.domain.resource.ResourceMetadataKeyStats;
import org.cbioportal.domain.resource.ResourceTableCounts;
import org.cbioportal.domain.resource.ResourceTableQuery;
import org.cbioportal.domain.resource.ResourceTableRow;
import org.cbioportal.domain.resource.ResourceTableTab;
import org.cbioportal.domain.resource.ResourceTabsRequest;

public interface ClickhouseResourceDataMapper {
  List<ResourceTableTab> getResourceTableTabs(@Param("request") ResourceTabsRequest request);

  List<ResourceTableRow> getResourceTableRows(@Param("query") ResourceTableQuery query);

  List<ResourceFacetOption> getResourceTableFacetValues(
      @Param("query") ResourceTableQuery query, @Param("column") String column);

  List<ResourceFacetOption> getResourceTableMetadataFacetValues(
      @Param("query") ResourceTableQuery query, @Param("metadataKey") String metadataKey);

  /**
   * Per-key stats (non-blank count, numeric-parseable count, min/max) used to decide whether a
   * metadata key should be treated as numeric or categorical. Computed in one pass over the current
   * tab's rows (before any column-level filters), see {@link
   * org.cbioportal.infrastructure.repository.clickhouse.resource.ClickhouseResourceDataRepository}.
   */
  List<ResourceMetadataKeyStats> getResourceTableMetadataKeyStats(
      @Param("query") ResourceTableQuery query);

  /**
   * The current resource tab's {@code resource_definition.custom_metadata} JSON schema, if any
   * study/row in scope has one set. Returns null when no override schema is present.
   */
  List<String> getResourceDefinitionCustomMetadata(@Param("query") ResourceTableQuery query);

  ResourceTableCounts getResourceTableCounts(@Param("query") ResourceTableQuery query);
}
