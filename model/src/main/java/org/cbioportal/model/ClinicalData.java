package org.cbioportal.model;

import java.io.Serializable;

public class ClinicalData implements Serializable {

    private Integer internalId;
    private String stableId;
    private String attrId;
    private String attrValue;
    private ClinicalAttribute clinicalAttribute;

    public Integer getInternalId() {
        return internalId;
    }

    public void setInternalId(Integer internalId) {
        this.internalId = internalId;
    }

    public String getStableId() {
        return stableId;
    }

    public void setStableId(String stableId) {
        this.stableId = stableId;
    }

    public String getAttrId() {
        return attrId;
    }

    public void setAttrId(String attrId) {
        this.attrId = attrId;
    }

    public String getAttrValue() {
        return attrValue;
    }

    public void setAttrValue(String attrValue) {
        this.attrValue = attrValue;
    }

    public ClinicalAttribute getClinicalAttribute() {
        return clinicalAttribute;
    }

    public void setClinicalAttribute(ClinicalAttribute clinicalAttribute) {
        this.clinicalAttribute = clinicalAttribute;
    }
}
