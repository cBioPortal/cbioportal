package org.cbioportal.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.cbioportal.model.util.Select;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class GeneFilterQuery implements Serializable {
    
    private String hugoGeneSymbol;
    private Integer entrezGeneId;
    private List<CNA> alterations;
    private boolean includeDriver;
    private boolean includeVUS;
    private boolean includeUnknownOncogenicity;
    private boolean includeGermline;
    private boolean includeSomatic;
    private boolean includeUnknownStatus;
    private Map<String, Boolean> selectedTiers;
    @JsonIgnore
    private Select<String> tiersSelect;
    private boolean includeUnknownTier;

    public GeneFilterQuery() {}

    public GeneFilterQuery(String hugoGeneSymbol,
                           Integer entrezGeneId,
                           List<CNA> alterations,
                           boolean includeDriver,
                           boolean includeVUS,
                           boolean includeUnknownOncogenicity,
                           Select<String> selectedTiers,
                           boolean includeUnknownTier,
                           boolean includeGermline,
                           boolean includeSomatic,
                           boolean includeUnknownStatus) {
        this.hugoGeneSymbol = hugoGeneSymbol;
        this.entrezGeneId = entrezGeneId;
        this.alterations = alterations;
        this.includeDriver = includeDriver;
        this.includeVUS = includeVUS;
        this.includeUnknownOncogenicity = includeUnknownOncogenicity;
        this.includeUnknownTier = includeUnknownTier;
        this.includeGermline = includeGermline;
        this.includeSomatic = includeSomatic;
        this.includeUnknownStatus = includeUnknownStatus;
        this.tiersSelect = selectedTiers;
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

    public boolean getIncludeDriver() {
        return includeDriver;
    }

    public void setIncludeDriver(boolean includeDriver) {
        this.includeDriver = includeDriver;
    }

    public boolean getIncludeVUS() {
        return includeVUS;
    }

    public void setIncludeVUS(boolean includeVUS) {
        this.includeVUS = includeVUS;
    }

    public boolean getIncludeUnknownOncogenicity() {
        return includeUnknownOncogenicity;
    }

    public void setIncludeUnknownOncogenicity(boolean includeUnknownOncogenicity) {
        this.includeUnknownOncogenicity = includeUnknownOncogenicity;
    }

    public boolean getIncludeGermline() {
        return includeGermline;
    }

    public void setIncludeGermline(boolean includeGermline) {
        this.includeGermline = includeGermline;
    }

    public boolean getIncludeSomatic() {
        return includeSomatic;
    }

    public void setIncludeSomatic(boolean includeSomatic) {
        this.includeSomatic = includeSomatic;
    }

    public boolean getIncludeUnknownStatus() {
        return includeUnknownStatus;
    }

    public void setIncludeUnknownStatus(boolean includeUnknownStatus) {
        this.includeUnknownStatus = includeUnknownStatus;
    }

    public Select<String> getSelectedTiers() {
        return tiersSelect;
    }

    public void setSelectedTiers(Map<String, Boolean> selectedTiers) {
        if (selectedTiers == null)
            this.tiersSelect = Select.none();
        else
            this.tiersSelect = Select.byValues(
                selectedTiers.entrySet().stream()
                    .filter(e -> e.getValue())
                    .map(e -> e.getKey()));
            if (selectedTiers.entrySet().stream().allMatch(e -> e.getValue()))
                this.tiersSelect.hasAll(true);
    }

    public boolean getIncludeUnknownTier() {
        return includeUnknownTier;
    }

    public void setIncludeUnknownTier(boolean includeUnknownTier) {
        this.includeUnknownTier = includeUnknownTier;
    }
    
}
