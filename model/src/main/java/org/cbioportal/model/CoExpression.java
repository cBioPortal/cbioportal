package org.cbioportal.model;

import java.io.Serializable;
import java.math.BigDecimal;
import javax.validation.constraints.NotNull;

public class CoExpression implements Serializable {

    public enum GeneticEntityType {

        GENE, GENESET
    }

    @NotNull
    private String geneticEntityId;
    @NotNull
    private GeneticEntityType geneticEntityType;
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

    public GeneticEntityType getGeneticEntityType() {
        return geneticEntityType;
    }

    public void setGeneticEntityType(GeneticEntityType geneticEntityType) {
        this.geneticEntityType = geneticEntityType;
    }

    public BigDecimal getSpearmansCorrelation() {
        return spearmansCorrelation;
    }

    public void setSpearmansCorrelation(BigDecimal spearmansCorrelation) {
        this.spearmansCorrelation = spearmansCorrelation;
    }

    public BigDecimal getpValue() {
        return pValue;
    }

    public void setpValue(BigDecimal pValue) {
        this.pValue = pValue;
    }
}
