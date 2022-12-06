package org.cbioportal.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.lang.Nullable;

public class StructuralVariantGeneSubQuery {

    @Nullable
    protected String hugoSymbol;
    
    @Nullable
    protected Integer entrezId;

    @Nullable
    protected StructuralVariantSpecialValue specialValue;

    public StructuralVariantGeneSubQuery() {}

    public StructuralVariantGeneSubQuery(String hugoSymbol) {
        this.hugoSymbol = hugoSymbol;
    }

    public StructuralVariantGeneSubQuery(String hugoSymbol, Integer entrezId) {
        this.entrezId = entrezId;
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

    @JsonIgnore
    public void setEntrezId(@Nullable Integer entrezId) {
        this.entrezId = entrezId;
    }
}

