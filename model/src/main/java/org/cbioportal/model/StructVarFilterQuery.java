package org.cbioportal.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.cbioportal.model.util.Select;
import org.springframework.lang.Nullable;

import java.io.Serializable;

public class StructVarFilterQuery extends BaseAlterationFilter implements Serializable {
    
    private StringStructuralVariantGeneSubQuery gene1HugoGeneSymbol;
    private StringStructuralVariantGeneSubQuery gene2HugoGeneSymbol;
    
    private Integer gene1EntrezGeneId;
    private Integer gene2EntrezGeneId;

    public StructVarFilterQuery() {}

    public StructVarFilterQuery(@Nullable String gene1HugoGeneSymbol,
                                @Nullable String gene2HugoGeneSymbol,
                                boolean includeDriver,
                                boolean includeVUS,
                                boolean includeUnknownOncogenicity,
                                Select<String> tiersSelect,
                                boolean includeUnknownTier,
                                boolean includeGermline,
                                boolean includeSomatic,
                                boolean includeUnknownStatus) {
        super(includeDriver, includeVUS, includeUnknownOncogenicity, includeGermline, includeSomatic, includeUnknownStatus, tiersSelect, includeUnknownTier);
        this.gene1HugoGeneSymbol = new StringStructuralVariantGeneSubQuery(gene1HugoGeneSymbol);
        this.gene2HugoGeneSymbol = new StringStructuralVariantGeneSubQuery(gene2HugoGeneSymbol);
    }

    public StringStructuralVariantGeneSubQuery getGene1HugoGeneSymbol() {
        return gene1HugoGeneSymbol;
    }

    public void setGene1HugoGeneSymbol(StringStructuralVariantGeneSubQuery gene1HugoGeneSymbol) {
        this.gene1HugoGeneSymbol = gene1HugoGeneSymbol;
    }

    public StringStructuralVariantGeneSubQuery getGene2HugoGeneSymbol() {
        return gene2HugoGeneSymbol;
    }

    public void setGene2HugoGeneSymbol(StringStructuralVariantGeneSubQuery gene2HugoGeneSymbol) {
        this.gene2HugoGeneSymbol = gene2HugoGeneSymbol;
    }
    
    @JsonIgnore
    public Integer getGene1EntrezGeneId() {
        return gene1EntrezGeneId;
    }

    @JsonIgnore
    public void setGene1EntrezGeneId(Integer gene1EntrezGeneId) {
        this.gene1EntrezGeneId = gene1EntrezGeneId;
    }

    @JsonIgnore
    public Integer getGene2EntrezGeneId() {
        return gene2EntrezGeneId;
    }

    @JsonIgnore
    public void setGene2EntrezGeneId(Integer gene2EntrezGeneId) {
        this.gene2EntrezGeneId = gene2EntrezGeneId;
    }
}
