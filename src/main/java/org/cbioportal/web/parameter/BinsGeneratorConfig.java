package org.cbioportal.web.parameter;

import java.math.BigDecimal;

public class BinsGeneratorConfig {

    private BigDecimal binSize;
    private BigDecimal anchorValue;

    public BigDecimal getBinSize() {
        return binSize;
    }

    public void setBinSize(BigDecimal binSize) {
        this.binSize = binSize;
    }

    public BigDecimal getAnchorValue() {
        return anchorValue;
    }

    public void setAnchorValue(BigDecimal anchorValue) {
        this.anchorValue = anchorValue;
    }

}
