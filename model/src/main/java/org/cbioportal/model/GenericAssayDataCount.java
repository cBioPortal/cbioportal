package org.cbioportal.model;

import java.io.Serializable;

public class GenericAssayDataCount implements Serializable {

    private String value;
    private Integer count;

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

