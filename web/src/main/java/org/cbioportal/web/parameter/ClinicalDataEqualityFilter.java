package org.cbioportal.web.parameter;

import java.io.Serializable;
import java.util.List;

public class ClinicalDataEqualityFilter extends ClinicalDataFilter  implements Serializable {

    private List<String> values;

    public List<String> getValues() {
        return values;
    }

    public void setValues(List<String> values) {
        this.values = values;
    }
}
