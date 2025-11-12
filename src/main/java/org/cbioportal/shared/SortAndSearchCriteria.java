package org.cbioportal.shared;

public record SortAndSearchCriteria(
    String searchTerm, String sortField, String sortOrder, Integer pageSize, Integer pageNumber) {
  boolean isSortable() {
    return (sortField != null && !sortField.isEmpty())
        && (sortOrder != null && !sortOrder.isEmpty());
  }

  boolean isSearchable() {
    return searchTerm != null && !searchTerm.isEmpty();
  }

  boolean isPaginated() {
    return pageSize != null;
  }

  /** Computed property for the SQL offset (uses PaginationCalculator). */
  public Integer offset() {
    return PaginationCalculator.offset(pageSize, pageNumber);
  }

  /** Computed property for the last index (useful for subList). */
  Integer getLastIndex(int listLength) {
    return PaginationCalculator.lastIndex(offset(), pageSize, listLength);
  }
}
