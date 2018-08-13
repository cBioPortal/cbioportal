package org.cbioportal.web.parameter;

import javax.validation.Valid;
import java.util.List;

public class ClinicalDataIntervalFilter extends ClinicalDataFilter {
    @Valid
    private List<ClinicalDataIntervalFilterValue> values;

    public List<ClinicalDataIntervalFilterValue> getValues() {
        return values;
    }

    public void setValues(List<ClinicalDataIntervalFilterValue> values) {
        this.values = values;
    }
}
