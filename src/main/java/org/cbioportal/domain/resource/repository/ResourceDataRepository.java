package org.cbioportal.domain.resource.repository;

import java.util.List;
import org.cbioportal.domain.resource.ResourceTableQuery;
import org.cbioportal.domain.resource.ResourceTableRow;
import org.cbioportal.domain.resource.ResourceTableTab;
import org.cbioportal.domain.resource.ResourceTabsRequest;

public interface ResourceDataRepository {
  List<ResourceTableTab> getResourceTableTabs(ResourceTabsRequest request);

  List<ResourceTableRow> getResourceTableRows(ResourceTableQuery query);

  long getResourceTableRowCount(ResourceTableQuery query);

  long getResourceTablePatientCount(ResourceTableQuery query);

  long getResourceTableSampleCount(ResourceTableQuery query);
}
