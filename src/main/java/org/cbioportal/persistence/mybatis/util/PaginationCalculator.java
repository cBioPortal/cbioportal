package org.cbioportal.persistence.mybatis.util;

public class PaginationCalculator {

    private PaginationCalculator() {}
    
    /**
     * PageNumber '0' represents the first page (no offset).
     */
    public static Integer offset(Integer pageSize, Integer pageNumber) {
        return pageSize == null || pageNumber == null ?
            null : pageSize * pageNumber;
    }
    
    /**
     * Returns 'lastIndex' as used by the subList command; position of the last element (exclusive).
     */
    public static Integer lastIndex(Integer offset, Integer pageSize, Integer listLength) {
        if (offset == null || pageSize == null || listLength == null) {
            return null;
        }
        return (offset + pageSize) <= listLength ?
            (offset + pageSize) : listLength;
    }

}
