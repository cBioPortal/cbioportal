package org.cbioportal.model;

import java.io.Serializable;
import java.util.Objects;

public class GenomicDataCount implements Serializable {

    private String label;
    private String value;
    private Integer count;
    private Integer uniqueCount;

    public GenomicDataCount() {}

    public GenomicDataCount(String label, String value, Integer count) {
        this.label = label;
        this.value = value;
        this.count = count;
    }
    
    public GenomicDataCount(String label, String value, Integer count, Integer uniqueCount) {
        this.label = label;
        this.value = value;
        this.count = count;
        this.uniqueCount = uniqueCount;
    }
    
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

    public Integer getUniqueCount() { return uniqueCount; }

    public void setUniqueCount(Integer uniqueCount) { this.uniqueCount = uniqueCount; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GenomicDataCount that = (GenomicDataCount) o;
        return Objects.equals(label, that.label) && Objects.equals(value, that.value) && Objects.equals(count, that.count) && Objects.equals(uniqueCount, that.uniqueCount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(label, value, count, uniqueCount);
    }
}
