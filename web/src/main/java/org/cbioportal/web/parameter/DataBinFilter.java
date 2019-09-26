package org.cbioportal.web.parameter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

import javax.validation.constraints.AssertTrue;

public class DataBinFilter implements Serializable {

    private Boolean disableLogScale = false;
    private List<BigDecimal> customBins;
    private BigDecimal start;
    private BigDecimal end;

    public Boolean getDisableLogScale() {
        return disableLogScale;
    }

    public void setDisableLogScale(Boolean disableLogScale) {
        this.disableLogScale = disableLogScale;
    }

    public List<BigDecimal> getCustomBins() {
        return customBins;
    }

    public void setCustomBins(List<BigDecimal> customBins) {
        this.customBins = customBins;
    }

    public BigDecimal getStart() {
        return start;
    }

    public void setStart(BigDecimal start) {
        this.start = start;
    }

    public BigDecimal getEnd() {
        return end;
    }

    public void setEnd(BigDecimal end) {
        this.end = end;
    }

    // TODO: make this work
    @AssertTrue
    private boolean rangeIsCoveringCustomBins() {
        if (this.customBins != null && (start != null || end != null)) {
            boolean valid = true;
            for (BigDecimal bin : this.customBins) {
                if (start != null && start.compareTo(bin) == 1) {
                    valid = false;
                    break;
                }
                if (end != null && end.compareTo(bin) == -1) {
                    valid = false;
                    break;
                }
            }
            return valid;
        }
        return true;
    }
}
