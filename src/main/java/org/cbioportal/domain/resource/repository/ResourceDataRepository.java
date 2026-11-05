package org.cbioportal.domain.resource.repository;

import java.util.List;
import org.cbioportal.domain.resource.ResourceQueryContext;
import org.cbioportal.domain.resource.ResourceScope;
import org.cbioportal.domain.resource.ResourceTableCounts;
import org.cbioportal.domain.resource.ResourceTableGroupPage;
import org.cbioportal.domain.resource.ResourceTableMetadataView;
import org.cbioportal.domain.resource.ResourceTableQuery;
import org.cbioportal.domain.resource.ResourceTableRow;
import org.cbioportal.domain.resource.ResourceTableTab;
import org.cbioportal.domain.resource.ResourceTabsRequest;

/**
 * Reads the resource table. Each read takes the query and the {@link ResourceQueryContext} it
 * resolved to; {@link ResourceQueryContext#none()} for a query whose cohort is identifier lists and
 * that reads one resource without grouping.
 */
public interface ResourceDataRepository {
  List<ResourceTableTab> getResourceTableTabs(ResourceTabsRequest request);

  List<ResourceTableRow> getResourceTableRows(
      ResourceTableQuery query, ResourceQueryContext context);

  /**
   * Row, patient and sample counts for the filtered set, in one pass. They count rows whether or
   * not the query groups them.
   */
  ResourceTableCounts getResourceTableCounts(
      ResourceTableQuery query, ResourceQueryContext context);

  /**
   * Metadata column info, categorical facets and numeric ranges together. They share the contract
   * and key-stats lookups, so resolving them in one call is what keeps a request from re-fetching
   * the same two things for every question it asks. For a grouped query a facet counts the groups
   * carrying each value rather than the rows; everything else is read as without grouping.
   */
  ResourceTableMetadataView getResourceTableMetadata(
      ResourceTableQuery query, ResourceQueryContext context);

  /** One page of groups, with the totals and, if asked for, the located group's position. */
  ResourceTableGroupPage getResourceTableGroups(
      ResourceTableQuery query, ResourceQueryContext context);

  /** The query's resources as its studies define and hold them; see {@link ResourceScope}. */
  ResourceScope getResourceScope(ResourceTableQuery query);
}
