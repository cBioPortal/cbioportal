package org.cbioportal.model;

import java.io.Serializable;
import org.cbioportal.model.ClinicalDataCountItem.ClinicalDataType;

public class DataBin implements Serializable {

    private String attributeId;
    private ClinicalDataType clinicalDataType;
    private String specialValue;
    private Double start;
    private Double end;
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

    public Integer getCount() {
		return count;
	}

	public void setCount(Integer count) {
		this.count = count;
	}
}
