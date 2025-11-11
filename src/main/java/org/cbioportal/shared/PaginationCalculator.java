package org.cbioportal.shared;

public final class PaginationCalculator {

  private PaginationCalculator() {
    // Utility class; prevent instantiation
  }

  /**
   * Computes the SQL-style offset (0-based index).
   *
   * @param pageSize number of items per page
   * @param pageNumber current page number (1-based; use 1 for first page)
   * @return offset (number of items to skip), or null if inputs are null
   */
  public static Integer offset(Integer pageSize, Integer pageNumber) {
    if (pageSize == null) return null;
    if (pageNumber == null) return 0;
    if (pageNumber < 1) throw new IllegalArgumentException("pageNumber must be >= 1");
    return pageSize * (pageNumber - 1);
  }

  /**
   * Computes the last index (exclusive) for a subList operation.
   *
   * @param offset starting index
   * @param pageSize number of items per page
   * @param listLength total size of the list
   * @return end index (exclusive), or null if any argument is null
   */
  public static Integer lastIndex(Integer offset, Integer pageSize, Integer listLength) {
    if (offset == null || pageSize == null || listLength == null) return null;
    return Math.min(offset + pageSize, listLength);
  }
}
