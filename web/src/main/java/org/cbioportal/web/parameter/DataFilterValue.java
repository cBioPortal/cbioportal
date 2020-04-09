package org.cbioportal.web.parameter;

import java.io.Serializable;
import java.math.BigDecimal;

public class DataFilterValue implements Serializable {

    private BigDecimal start;
    private BigDecimal end;
    private String value;

    public BigDecimal getStart() {
        return start;
    }

    public void setStart(BigDecimal start) {
        this.start = start;
    }

    public BigDecimal getEnd() {
        return end;
    }

    public void setEnd(BigDecimal end) {
        this.end = end;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
