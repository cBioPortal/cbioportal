package org.cbioportal.web.parameter;

import javax.validation.constraints.AssertTrue;
import java.io.Serializable;

public class ClinicalDataIntervalFilterValue implements Serializable
{
    private Double start;
    private Double end;
    private String value;

    @AssertTrue
    public boolean isEitherValueOrRangePresent() {
        return value != null ^ (start != null || end != null);
    }
    
    public Double getStart() {
        return start;
    }

    public void setStart(Double start) {
        this.start = start;
    }

    public Double getEnd() {
        return end;
    }

    public void setEnd(Double end) {
        this.end = end;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
