package org.cbioportal.model;

import java.io.Serializable;

import org.cbioportal.model.ClinicalDataCountItem.ClinicalDataType;

public class ClinicalAttributeCount implements Serializable {

    private String attrId;
    private Integer count;

    public String getAttrId() {
        return attrId;
    }

    public void setAttrId(String attrId) {
        this.attrId = attrId;
    }

	public Integer getCount() {
		return count;
	}

	public void setCount(Integer count) {
		this.count = count;
	}
}
