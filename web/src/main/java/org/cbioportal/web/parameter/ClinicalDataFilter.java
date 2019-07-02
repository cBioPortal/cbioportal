package org.cbioportal.web.parameter;

import java.io.Serializable;

import org.cbioportal.model.ClinicalDataCountItem.ClinicalDataType;

public class ClinicalDataFilter  implements Serializable
{
    private String attributeId;
    private ClinicalDataType clinicalDataType;

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
}
