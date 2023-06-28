package org.cbioportal.model;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

public class GenericAssayBinaryEnrichment extends GenericAssayEnrichment {
    @NotNull
    private List<GenericAssayCountSummary> counts;
    @NotNull
    private BigDecimal qValue;

    public List<GenericAssayCountSummary> getCounts() {
        return counts;
    }

    public void setCounts(List<GenericAssayCountSummary> counts) {
        this.counts = counts;
    }

    public BigDecimal getqValue() {
        return qValue;
    }

    public void setqValue(BigDecimal qValue) {
        this.qValue = qValue;
    }
}
