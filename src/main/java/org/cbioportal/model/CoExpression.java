package org.cbioportal.model;

import java.io.Serializable;
import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

public class CoExpression implements Serializable {

    @NotNull
    private String geneticEntityId;
    @NotNull
    private EntityType geneticEntityType;
    @NotNull
    private BigDecimal spearmansCorrelation;
    @NotNull
    private BigDecimal pValue;

    public String getGeneticEntityId() {
        return geneticEntityId;
    }

    public void setGeneticEntityId(String geneticEntityId) {
        this.geneticEntityId = geneticEntityId;
    }
    
    public EntityType getGeneticEntityType() {
        return geneticEntityType;
    }

    public void setGeneticEntityType(EntityType geneticEntityType) {
        this.geneticEntityType = geneticEntityType;
    }

    public BigDecimal getSpearmansCorrelation() {
        return spearmansCorrelation;
    }

    public void setSpearmansCorrelation(BigDecimal spearmansCorrelation) {
        this.spearmansCorrelation = spearmansCorrelation;
    }

    @JsonProperty("pValue")
    public BigDecimal getpValue() {
        return pValue;
    }

    public void setpValue(BigDecimal pValue) {
        this.pValue = pValue;
    }
}
