package org.cbioportal.domain.resource.repository;

import java.util.List;
import java.util.Map;
import org.cbioportal.domain.resource.ResourceColumnInfo;
import org.cbioportal.domain.resource.ResourceFacetOption;
import org.cbioportal.domain.resource.ResourceNumericRange;
import org.cbioportal.domain.resource.ResourceTableQuery;
import org.cbioportal.domain.resource.ResourceTableRow;
import org.cbioportal.domain.resource.ResourceTableTab;
import org.cbioportal.domain.resource.ResourceTabsRequest;

public interface ResourceDataRepository {
  List<ResourceTableTab> getResourceTableTabs(ResourceTabsRequest request);

  List<ResourceTableRow> getResourceTableRows(ResourceTableQuery query);

  Map<String, List<ResourceFacetOption>> getResourceTableFacets(ResourceTableQuery query);

  Map<String, ResourceNumericRange> getResourceTableFacetRanges(ResourceTableQuery query);

  /**
   * Presentation info for the dynamic metadata columns discovered in the current tab, in display
   * order, merging auto-detection with the optional {@code resource_definition.custom_metadata}
   * contract.
   */
  List<ResourceColumnInfo> getResourceTableMetadataColumns(ResourceTableQuery query);

  long getResourceTableRowCount(ResourceTableQuery query);

  long getResourceTablePatientCount(ResourceTableQuery query);

  long getResourceTableSampleCount(ResourceTableQuery query);
}
