package org.cbioportal.model;

import org.cbioportal.model.util.Select;
import org.springframework.lang.Nullable;

import java.io.Serializable;

public class StructuralVariantFilterQuery extends BaseAlterationFilter implements Serializable {
    
    private StructuralVariantGeneSubQuery gene1Query;
    private StructuralVariantGeneSubQuery gene2Query;
    
    public StructuralVariantFilterQuery() {}

    public StructuralVariantFilterQuery(String gene1HugoSymbol,
                                        @Nullable Integer gene1EntrezId,
                                        String gene2HugoSymbol,
                                        @Nullable Integer gene2EntrezId,
                                        boolean includeDriver,
                                        boolean includeVUS,
                                        boolean includeUnknownOncogenicity,
                                        Select<String> tiersSelect,
                                        boolean includeUnknownTier,
                                        boolean includeGermline,
                                        boolean includeSomatic,
                                        boolean includeUnknownStatus) {
        super(includeDriver, includeVUS, includeUnknownOncogenicity, includeGermline, includeSomatic, includeUnknownStatus, tiersSelect, includeUnknownTier);
        this.gene1Query = new StructuralVariantGeneSubQuery(gene1HugoSymbol, gene1EntrezId);
        this.gene2Query = new StructuralVariantGeneSubQuery(gene2HugoSymbol, gene2EntrezId);
    }

    public StructuralVariantGeneSubQuery getGene1Query() {
        return gene1Query;
    }

    public void setGene1Query(StructuralVariantGeneSubQuery gene1Query) {
        this.gene1Query = gene1Query;
    }

    public StructuralVariantGeneSubQuery getGene2Query() {
        return gene2Query;
    }

    public void setGene2Query(StructuralVariantGeneSubQuery gene2Query) {
        this.gene2Query = gene2Query;
    }
    
}
