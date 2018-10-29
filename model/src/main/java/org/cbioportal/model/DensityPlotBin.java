package org.cbioportal.model;

import java.io.Serializable;
import java.math.BigDecimal;

public class DensityPlotBin implements Serializable {

    private Integer count;
    private BigDecimal x;
    private BigDecimal y;

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public BigDecimal getX() {
        return x;
    }

    public void setX(BigDecimal x) {
        this.x = x;
    }

    public BigDecimal getY() {
        return y;
    }

    public void setY(BigDecimal y) {
        this.y = y;
    }
}
