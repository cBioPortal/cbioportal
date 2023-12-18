package org.cbioportal.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class GenericAssayCategoricalEnrichment extends GenericAssayEnrichment {
    @NotNull
    private BigDecimal qValue;

    @JsonProperty("qValue")
    public BigDecimal getqValue() {
        return qValue;
    }

    public void setqValue(BigDecimal qValue) {
        this.qValue = qValue;
    }

}
