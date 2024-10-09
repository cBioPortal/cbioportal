package org.cbioportal.model.enclave;

import java.math.BigDecimal;

public class Range {
    public Range(Integer start, Integer end) {
        this.start = (start == null ? null : BigDecimal.valueOf(start));
        this.end = (end == null ? null : BigDecimal.valueOf(end));
    }
    
    public Range(BigDecimal start, BigDecimal end) {
        this.start = start;
        this.end = end;
    }
    
    public BigDecimal start; // exclusive
    public BigDecimal end; // inclusive
}
