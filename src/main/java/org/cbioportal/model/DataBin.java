package org.cbioportal.model;

import java.io.Serializable;
import java.math.BigDecimal;

public class DataBin implements Serializable {

    private String specialValue;
    private BigDecimal start;
    private BigDecimal end;
    private Integer count;

    public String getSpecialValue() {
        return specialValue;
    }

    public void setSpecialValue(String specialValue) {
        this.specialValue = specialValue;
    }

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

    public Integer getCount() {
		return count;
	}

	public void setCount(Integer count) {
		this.count = count;
	}
}
