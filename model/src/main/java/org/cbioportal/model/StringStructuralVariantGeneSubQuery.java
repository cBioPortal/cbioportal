package org.cbioportal.model;

import org.springframework.lang.Nullable;

public class StringStructuralVariantGeneSubQuery {

    @Nullable
    protected String geneId;

    @Nullable
    protected StructuralVariantSpecialValue specialValue;

    public StringStructuralVariantGeneSubQuery(String hugoGeneSymbol) {
        geneId = hugoGeneSymbol;
    }

    @Nullable
    public String getGeneId() {
        return geneId;
    }

    public void setGeneId(@Nullable String geneId) {
        this.geneId = geneId;
    }

    @Nullable
    public StructuralVariantSpecialValue getSpecialValue() {
        return specialValue;
    }

    public void setSpecialValue(@Nullable StructuralVariantSpecialValue specialValue) {
        this.specialValue = specialValue;
    }
}

