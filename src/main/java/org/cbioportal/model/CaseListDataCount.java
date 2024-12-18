package org.cbioportal.model;

import java.io.Serializable;

public class CaseListDataCount implements Serializable {

    private String label;
    private String value;
    private Integer count;

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

}
