package org.cbioportal.persistence.mybatis.util;

import org.springframework.stereotype.Component;

@Component
public class OffsetCalculator {

    public Integer calculate(Integer pageSize, Integer pageNumber) {
        return pageSize == null || pageNumber == null
            ? null
            : pageSize * pageNumber;
    }
}
