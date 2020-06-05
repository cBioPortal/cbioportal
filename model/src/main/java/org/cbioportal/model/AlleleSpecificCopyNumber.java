package org.cbioportal.model;

import java.io.Serializable;

public class AlleleSpecificCopyNumber implements Serializable {

    private Integer ascnIntegerCopyNumber;
    private String ascnMethod;
    private Float ccfExpectedCopiesUpper;
    private Float ccfExpectedCopies;
    private String clonal;
    private Integer minorCopyNumber;
    private Integer expectedAltCopies;
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

    public Float getCcfExpectedCopiesUpper() {
        return ccfExpectedCopiesUpper;
    }

    public void setCcfExpectedCopiesUpper(Float ccfExpectedCopiesUpper) {
        this.ccfExpectedCopiesUpper = ccfExpectedCopiesUpper;
    }

    public Float getCcfExpectedCopies() {
        return ccfExpectedCopies;
    }

    public void setCcfExpectedCopies(Float ccfExpectedCopies) {
        this.ccfExpectedCopies = ccfExpectedCopies;
    }

    public String getClonal() {
        return clonal;
    }

    public void setClonal(String clonal) {
        this.clonal = clonal;
    }

    public Integer getMinorCopyNumber() {
        return minorCopyNumber;
    }

    public void setMinorCopyNumber(Integer minorCopyNumber) {
        this.minorCopyNumber = minorCopyNumber;
    }

    public Integer getExpectedAltCopies() {
        return expectedAltCopies;
    }

    public void setExpectedAltCopies(Integer expectedAltCopies) {
        this.expectedAltCopies = expectedAltCopies;
    }

    public Integer getTotalCopyNumber() {
        return totalCopyNumber;
    }

    public void setTotalCopyNumber(Integer totalCopyNumber) {
        this.totalCopyNumber = totalCopyNumber;
    }
}
