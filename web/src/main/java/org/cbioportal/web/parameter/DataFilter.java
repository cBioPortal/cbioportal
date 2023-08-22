package org.cbioportal.web.parameter;

import java.io.Serializable;
import java.util.List;

public class DataFilter implements Serializable {

    private List<DataFilterValue> values;

    public List<DataFilterValue> getValues() {
        return values;
    }

    public void setValues(List<DataFilterValue> values) {
        this.values = values;
    }

}
