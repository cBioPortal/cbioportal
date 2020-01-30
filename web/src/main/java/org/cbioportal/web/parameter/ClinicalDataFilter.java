package org.cbioportal.web.parameter;

import java.io.Serializable;
import java.util.List;

public class ClinicalDataFilter implements Serializable {
    private String attributeId;

    private List<ClinicalDataFilterValue> values;

    public String getAttributeId() {
        return attributeId;
    }

    public void setAttributeId(String attributeId) {
        this.attributeId = attributeId;
    }

    public List<ClinicalDataFilterValue> getValues() {
        return values;
    }

    public void setValues(List<ClinicalDataFilterValue> values) {
        this.values = values;
    }
}
