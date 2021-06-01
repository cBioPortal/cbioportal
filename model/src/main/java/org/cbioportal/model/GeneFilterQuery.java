package org.cbioportal.model;

import org.cbioportal.model.util.Select;

import java.io.Serializable;
import java.util.List;

public class GeneFilterQuery extends BaseAlterationFilter implements Serializable {
    
    private String hugoGeneSymbol;
    private Integer entrezGeneId;
    private List<CNA> alterations;

    public GeneFilterQuery() {}

    public GeneFilterQuery(String hugoGeneSymbol,
                           Integer entrezGeneId,
                           List<CNA> alterations,
                           boolean includeDriver,
                           boolean includeVUS,
                           boolean includeUnknownOncogenicity,
                           Select<String> tiersSelect,
                           boolean includeUnknownTier,
                           boolean includeGermline,
                           boolean includeSomatic,
                           boolean includeUnknownStatus) {
        super(includeDriver, includeVUS, includeUnknownOncogenicity, includeGermline, includeSomatic, includeUnknownStatus, tiersSelect, includeUnknownTier);
        this.hugoGeneSymbol = hugoGeneSymbol;
        this.entrezGeneId = entrezGeneId;
        this.alterations = alterations;
    }

    public String getHugoGeneSymbol() {
        return hugoGeneSymbol;
    }

    public void setHugoGeneSymbol(String hugoGeneSymbol) {
        this.hugoGeneSymbol = hugoGeneSymbol;
    }

    public Integer getEntrezGeneId() {
        return entrezGeneId;
    }

    public void setEntrezGeneId(int entrezGeneId) {
        this.entrezGeneId = entrezGeneId;
    }

    public List<CNA> getAlterations() {
        return alterations;
    }

    public void setAlterations(List<CNA> alterations) {
        this.alterations = alterations;
    }
    
}
