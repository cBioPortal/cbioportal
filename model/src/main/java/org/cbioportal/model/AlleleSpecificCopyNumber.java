package org.cbioportal.model;

import java.io.Serializable;

public class AlleleSpecificCopyNumber implements Serializable {
    private Integer ascnIntegerCopyNumber;
    private String ascnMethod;
    private Float ccfMCopiesUpper;
    private Float ccfMCopies;
    private Boolean clonal;
    private Integer minorCopyNumber;
    private Integer mutantCopies;
    private Integer totalCopyNumber;

    public Integer getAscnIntegerCopyNumber() {
        return ascnIntegerCopyNumber;
    }

    public void setAscnIntegerCopyNumber(Integer ascnIntegerCopyNumber) {
        this.ascnIntegerCopyNumber = ascnIntegerCopyNumber;
    }

    public String getAscnMethod() {
        return ascnMethod;
    }

    public void setAscnMethod(String ascnMethod) {
        this.ascnMethod = ascnMethod;
    }

    public Float getCcfMCopiesUpper() {
        return ccfMCopiesUpper;
    }

    public void setCcfMCopiesUpper(Float ccfMCopiesUpper) {
        this.ccfMCopiesUpper = ccfMCopiesUpper;
    }

    public Float getCcfMCopies() {
        return ccfMCopies;
    }

    public void setCcfMCopies(Float ccfMCopies) {
        this.ccfMCopies = ccfMCopies;
    }

    public Boolean getClonal() {
        return clonal;
    }

    public void setClonal(Boolean clonal) {
        this.clonal = clonal;
    }

    public Integer getMinorCopyNumber() {
        return minorCopyNumber;
    }

    public void setMinorCopyNumber(Integer minorCopyNumber) {
        this.minorCopyNumber = minorCopyNumber;
    }

    public Integer getMutantCopies() {
        return mutantCopies;
    }

    public void setMutantCopies(Integer mutantCopies) {
        this.mutantCopies = mutantCopies;
    }

    public Integer getTotalCopyNumber() {
        return totalCopyNumber;
    }

    public void setTotalCopyNumber(Integer totalCopyNumber) {
        this.totalCopyNumber = totalCopyNumber;
    }
}
