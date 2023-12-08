package org.cbioportal.model;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class GenericAssayCategoricalEnrichment extends GenericAssayEnrichment {
    @NotNull
    private BigDecimal qValue;

    public BigDecimal getQValue() {
        return qValue;
    }

    public void setQValue(BigDecimal qValue) {
        this.qValue = qValue;
    }

}
