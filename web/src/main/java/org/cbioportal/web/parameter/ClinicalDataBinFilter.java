package org.cbioportal.web.parameter;

import com.google.common.collect.Range;

import javax.validation.constraints.AssertTrue;
import java.util.List;

public class ClinicalDataBinFilter extends ClinicalDataFilter
{
    private Boolean disableLogScale = false;

    private List<Double> customBins;

    private Double start;
    
    private Double end;

    public Boolean getDisableLogScale() {
        return disableLogScale;
    }

    public void setDisableLogScale(Boolean disableLogScale) {
        this.disableLogScale = disableLogScale;
    }

    public List<Double> getCustomBins() {
        return customBins;
    }

    public void setCustomBins(List<Double> customBins) {
        this.customBins = customBins;
    }

    public Double getStart() {
        return start;
    }

    public void setStart(Double start) {
        this.start = start;
    }

    public Double getEnd() {
        return end;
    }

    public void setEnd(Double end) {
        this.end = end;
    }

    // TODO: make this work
    @AssertTrue
    private boolean rangeIsCoveringCustomBins() {
        if (this.customBins != null && (start != null || end != null)) {
            boolean valid = true;
            for (Double bin : this.customBins) {
                if(start != null && start > bin) {
                    valid = false;
                    break;
                }
                if(end != null && end < bin) {
                    valid = false;
                    break;
                }
            }
            return valid;
        }
        return true;
    }
}
