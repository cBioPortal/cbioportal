package org.cbioportal.web.parameter;

import java.math.BigDecimal;

public class RectangleBounds {

    private BigDecimal xStart;
    private BigDecimal xEnd;
    private BigDecimal yStart;
    private BigDecimal yEnd;

    public BigDecimal getxStart() {
        return xStart;
    }

    public void setxStart(BigDecimal xStart) {
        this.xStart = xStart;
    }

    public BigDecimal getyEnd() {
        return yEnd;
    }

    public void setyEnd(BigDecimal yEnd) {
        this.yEnd = yEnd;
    }

    public BigDecimal getyStart() {
        return yStart;
    }

    public void setyStart(BigDecimal yStart) {
        this.yStart = yStart;
    }

    public BigDecimal getxEnd() {
        return xEnd;
    }

    public void setxEnd(BigDecimal xEnd) {
        this.xEnd = xEnd;
    }
}
