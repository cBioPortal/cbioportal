package org.cbioportal.model;

import java.io.Serializable;
import java.util.Objects;

public class GenomicDataCount implements Serializable {

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GenomicDataCount that = (GenomicDataCount) o;
        return label.equals(that.label) && value.equals(that.value) && count.equals(that.count);
    }

    @Override
    public int hashCode() {
        return Objects.hash(label, value, count);
    }
}
