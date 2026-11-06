package org.cbioportal.domain.resource;

import java.util.List;

/**
 * One page of a grouped resource table.
 *
 * @param totalGroups how many groups remain after filters and search, across every page
 * @param totalRows how many rows those groups hold together
 * @param locatedIndex the zero-based position of the requested {@code locate} group under the
 *     current filters and sort, or null when no group was asked for or it has been filtered out
 * @param groups the requested page of groups, in sort order
 */
public record ResourceTableGroupPage(
    long totalGroups, long totalRows, Long locatedIndex, List<ResourceTableGroup> groups) {

  public static ResourceTableGroupPage empty() {
    return new ResourceTableGroupPage(0L, 0L, null, List.of());
  }
}
