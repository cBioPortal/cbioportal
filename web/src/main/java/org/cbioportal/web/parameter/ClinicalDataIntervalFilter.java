package org.cbioportal.web.parameter;

import java.io.Serializable;
import java.util.List;

public class ClinicalDataIntervalFilter extends ClinicalDataFilter implements Serializable {

    private List<ClinicalDataIntervalFilterValue> values;

    public List<ClinicalDataIntervalFilterValue> getValues() {
        return values;
    }

    public void setValues(List<ClinicalDataIntervalFilterValue> values) {
        this.values = values;
    }
}
