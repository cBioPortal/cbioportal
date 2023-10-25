package org.cbioportal.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.lang.Nullable;

import jakarta.validation.constraints.AssertTrue;
import java.io.Serializable;

public class StructuralVariantGeneSubQuery implements Serializable {

    @Nullable
    protected String hugoSymbol;
    
    @Nullable
    protected Integer entrezId;

    @Nullable
    protected StructuralVariantSpecialValue specialValue;
    
    @JsonIgnore
    @AssertTrue(message = "Should contain only one EntrezId, hugoSymbol or specialValue.")
    public boolean isContainingOnlyOneIdentifierOrSpecialValue() {
        int fieldCount = 0;
        if (entrezId != null) {
            fieldCount++;
        }
        if (hugoSymbol != null) {
            fieldCount++;
        }
        if (specialValue != null) {
            fieldCount++;
        }
        return fieldCount == 1;
    }
    
    public StructuralVariantGeneSubQuery() {}

    public StructuralVariantGeneSubQuery(String hugoSymbol) {
        this.hugoSymbol = hugoSymbol;
    }

    public StructuralVariantGeneSubQuery(Integer entrezId) {
        this.entrezId = entrezId;
    }

    public StructuralVariantGeneSubQuery(StructuralVariantSpecialValue specialValue) {
        this.specialValue = specialValue;
    }

    public StructuralVariantGeneSubQuery(String hugoSymbol, Integer entrezId) {
        this.entrezId = entrezId;
        this.hugoSymbol = hugoSymbol;
        if (hugoSymbol == null && entrezId == null) {
            this.specialValue = StructuralVariantSpecialValue.NO_GENE;
        }
    }

    @Nullable
    public String getHugoSymbol() {
        return hugoSymbol;
    }

    public void setHugoSymbol(@Nullable String hugoSymbol) {
        this.hugoSymbol = hugoSymbol;
    }

    @Nullable
    public StructuralVariantSpecialValue getSpecialValue() {
        return specialValue;
    }

    public void setSpecialValue(@Nullable StructuralVariantSpecialValue specialValue) {
        this.specialValue = specialValue;
    }

    @Nullable
    public Integer getEntrezId() {
        return entrezId;
    }

    public void setEntrezId(@Nullable Integer entrezId) {
        this.entrezId = entrezId;
    }
}

