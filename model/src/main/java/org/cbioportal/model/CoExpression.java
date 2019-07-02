package org.cbioportal.model;

import java.io.Serializable;
import java.math.BigDecimal;
import javax.validation.constraints.NotNull;

public class CoExpression implements Serializable {

    public enum GeneticEntityType {

        GENE,
        GENESET
    }

    @NotNull
    private String geneticEntityId;
    @NotNull
    private String geneticEntityName;
    @NotNull
    private GeneticEntityType geneticEntityType;
    @NotNull
    private String cytoband;
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

    public String getGeneticEntityName() {
        return geneticEntityName;
    }

    public void setGeneticEntityName(String geneticEntityName) {
        this.geneticEntityName = geneticEntityName;
    }
    
    public GeneticEntityType getGeneticEntityType() {
        return geneticEntityType;
    }

    public void setGeneticEntityType(GeneticEntityType geneticEntityType) {
        this.geneticEntityType = geneticEntityType;
    }

    public String getCytoband() {
        return cytoband;
    }

    public void setCytoband(String cytoband) {
        this.cytoband = cytoband;
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
