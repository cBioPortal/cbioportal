package org.cbioportal.model;

import java.io.Serializable;
import java.math.BigDecimal;

public class DensityPlotBin implements Serializable {

    private Integer count;
    private BigDecimal binX;
    private BigDecimal binY;
    private BigDecimal minX;
    private BigDecimal maxX;
    private BigDecimal minY;
    private BigDecimal maxY;

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public BigDecimal getBinX() {
        return binX;
    }

    public void setBinX(BigDecimal binX) {
        this.binX = binX;
    }

    public BigDecimal getBinY() {
        return binY;
    }

    public void setBinY(BigDecimal binY) {
        this.binY = binY;
    }

    public BigDecimal getMinX() {
        return minX;
    }

    public void setMinX(BigDecimal minX) {
        this.minX = minX;
    }

    public BigDecimal getMaxX() {
        return maxX;
    }

    public void setMaxX(BigDecimal maxX) {
        this.maxX = maxX;
    }

    public BigDecimal getMinY() {
        return minY;
    }

    public void setMinY(BigDecimal minY) {
        this.minY = minY;
    }

    public BigDecimal getMaxY() {
        return maxY;
    }

    public void setMaxY(BigDecimal maxY) {
        this.maxY = maxY;
    }
}
