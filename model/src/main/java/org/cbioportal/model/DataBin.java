package org.cbioportal.model;

import java.io.Serializable;
import java.math.BigDecimal;

import org.cbioportal.model.ClinicalDataCountItem.ClinicalDataType;

public class DataBin implements Serializable {

    private String attributeId;
    private ClinicalDataType clinicalDataType;
    private String specialValue;
    private BigDecimal start;
    private BigDecimal end;
    private Integer count;

    public String getAttributeId() {
		return attributeId;
	}

	public void setAttributeId(String attributeId) {
		this.attributeId = attributeId;
	}

    public ClinicalDataType getClinicalDataType() {
        return clinicalDataType;
    }

    public void setClinicalDataType(ClinicalDataType clinicalDataType) {
        this.clinicalDataType = clinicalDataType;
    }

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
