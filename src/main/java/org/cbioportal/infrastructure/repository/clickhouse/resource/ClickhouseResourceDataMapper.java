package org.cbioportal.infrastructure.repository.clickhouse.resource;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.cbioportal.domain.resource.ResourceTableQuery;
import org.cbioportal.domain.resource.ResourceTableRow;
import org.cbioportal.domain.resource.ResourceTableTab;
import org.cbioportal.domain.resource.ResourceTabsRequest;

public interface ClickhouseResourceDataMapper {
  List<ResourceTableTab> getResourceTableTabs(@Param("request") ResourceTabsRequest request);

  List<ResourceTableRow> getResourceTableRows(@Param("query") ResourceTableQuery query);

  long getResourceTableRowCount(@Param("query") ResourceTableQuery query);

  long getResourceTablePatientCount(@Param("query") ResourceTableQuery query);

  long getResourceTableSampleCount(@Param("query") ResourceTableQuery query);
}
