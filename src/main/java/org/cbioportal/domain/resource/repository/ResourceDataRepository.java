package org.cbioportal.domain.resource.repository;

import java.util.List;
import org.cbioportal.domain.resource.ResourceTableCounts;
import org.cbioportal.domain.resource.ResourceTableMetadataView;
import org.cbioportal.domain.resource.ResourceTableQuery;
import org.cbioportal.domain.resource.ResourceTableTab;
import org.cbioportal.domain.resource.ResourceTabsRequest;
import org.cbioportal.domain.resource.StoredResourceTableRow;

public interface ResourceDataRepository {
  List<ResourceTableTab> getResourceTableTabs(ResourceTabsRequest request);

  /** One page of rows, each with its own resource definition's url_template. */
  List<StoredResourceTableRow> getResourceTableRows(ResourceTableQuery query);

  /** Row, patient and sample counts for the filtered set, in one pass. */
  ResourceTableCounts getResourceTableCounts(ResourceTableQuery query);

  /**
   * Metadata column info, categorical facets and numeric ranges together. They share the contract
   * and key-stats lookups, so resolving them in one call is what keeps a request from re-fetching
   * the same two things for every question it asks.
   */
  ResourceTableMetadataView getResourceTableMetadata(ResourceTableQuery query);
}
