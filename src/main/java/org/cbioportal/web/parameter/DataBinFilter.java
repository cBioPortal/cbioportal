package org.cbioportal.web.parameter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

import jakarta.validation.constraints.AssertTrue;
import org.springframework.validation.annotation.Validated;

@Validated
public class DataBinFilter implements Serializable {

    public enum BinMethod {
        MEDIAN,
        QUARTILE,
        CUSTOM,
        GENERATE;
    }

    private Boolean disableLogScale = false;
    private List<BigDecimal> customBins;                    // needed for 'Custom bins' frontend option
    
    // FIXME: Code added for backwards compatibility.
    // Replace by commented out line after merge of PR:
    // https://github.com/cBioPortal/cbioportal-frontend/pull/4102
    private BinMethod binMethod = BinMethod.CUSTOM;
    //private BinMethod binMethod;                           // needed for 'Median split' and 'Quartile' frontend options
    
    private BinsGeneratorConfig binsGeneratorConfig;          // needed for 'Generate Bins' frontend option
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

    public BinMethod getBinMethod() {
        return binMethod;
    }

    public void setBinMethod(BinMethod binMethod) {
        this.binMethod = binMethod;
    }

    public BinsGeneratorConfig getBinsGeneratorConfig() {
        return binsGeneratorConfig;
    }

    public void setBinsGeneratorConfig(
        BinsGeneratorConfig binsGeneratorConfig) {
        this.binsGeneratorConfig = binsGeneratorConfig;
    }

    // TODO: make this work
    @AssertTrue
    private boolean rangeIsCoveringCustomBins() {
        if (this.customBins != null && (start != null || end != null)) {
            boolean valid = true;
            for (BigDecimal bin : this.customBins) {
                if (start != null && start.compareTo(bin) > 0) {
                    valid = false;
                    break;
                }
                if (end != null && end.compareTo(bin) < 0) {
                    valid = false;
                    break;
                }
            }
            return valid;
        }
        return true;
    }
}
