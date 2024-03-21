package org.cbioportal.model;

import java.io.Serializable;
import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

public class ClinicalDataEnrichment implements Serializable {

    @NotNull
    private ClinicalAttribute clinicalAttribute;
    @NotNull
    private BigDecimal score;
    @NotNull
    private String method;
    @NotNull
    private BigDecimal pValue;

    public ClinicalAttribute getClinicalAttribute() {
        return clinicalAttribute;
    }

    public void setClinicalAttribute(ClinicalAttribute clinicalAttribute) {
        this.clinicalAttribute = clinicalAttribute;
    }

    public BigDecimal getScore() {
        return score;
    }

    public void setScore(BigDecimal score) {
        this.score = score;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    @JsonProperty("pValue")
    public BigDecimal getpValue() {
        return pValue;
    }

    public void setpValue(BigDecimal pValue) {
        this.pValue = pValue;
    }

}
