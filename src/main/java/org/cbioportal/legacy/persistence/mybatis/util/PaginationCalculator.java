package org.cbioportal.legacy.persistence.mybatis.util;

public class PaginationCalculator {

  private PaginationCalculator() {}

  /** PageNumber '0' represents the first page (no offset). */
  public static Integer offset(Integer pageSize, Integer pageNumber) {
    if (pageSize == null || pageNumber == null) {
      return null;
    }

    long calculatedOffset = (long) pageSize * pageNumber;
    return calculatedOffset > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) calculatedOffset;
  }

  /**
   * Returns 'lastIndex' as used by the subList command; position of the last element (exclusive).
   */
  public static Integer lastIndex(Integer offset, Integer pageSize, Integer listLength) {
    if (offset == null || pageSize == null || listLength == null) {
      return null;
    }

    long calculatedEnd = (long) offset + pageSize;
    return (int) Math.min(calculatedEnd, listLength);
  }
}
