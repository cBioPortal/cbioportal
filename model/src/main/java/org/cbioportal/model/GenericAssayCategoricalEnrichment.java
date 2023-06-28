package org.cbioportal.model;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Comparator;

public class GenericAssayCategoricalEnrichment extends GenericAssayEnrichment {
    @NotNull
    private BigDecimal qValue;

    public BigDecimal getqValue() {
        return qValue;
    }

    public void setqValue(BigDecimal qValue) {
        this.qValue = qValue;
    }
}
