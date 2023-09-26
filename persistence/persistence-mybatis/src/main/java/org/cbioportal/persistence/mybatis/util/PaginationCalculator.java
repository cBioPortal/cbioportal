package org.cbioportal.persistence.mybatis.util;

import org.springframework.stereotype.Component;

@Component
public class PaginationCalculator {

    public Integer offset(Integer pageSize, Integer pageNumber) {
        // PageNumber '0' represents the first page (no offset).
        return pageSize == null || pageNumber == null ? null : pageSize * pageNumber;
    }
    
    // 'lastIndex as used by the subList command. Returns position of the last element (exclusive).
    public Integer lastIndex(Integer offset, Integer pageSize, Integer listLength) {
        if (offset == null || pageSize == null || listLength == null) {
            return null;
        }
        return (offset + pageSize) <= listLength ?
            (offset + pageSize) : listLength;
    }

}
