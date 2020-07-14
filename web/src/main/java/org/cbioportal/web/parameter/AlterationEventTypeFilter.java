package org.cbioportal.web.parameter;

import org.cbioportal.model.CNA;
import org.cbioportal.model.MutationEventType;

import java.util.Map;

public class AlterationEventTypeFilter {
    
    private Map<MutationEventType, Boolean> mutationEventTypes;
    private Map<CNA, Boolean> copyNumberAlterationEventTypes;
    private Map<String,Boolean> selectedTiers;
    private boolean includeDriver;
    private boolean includeVUS;
    private boolean includeUnknownOncogenicity;
    private boolean includeUnknownTier;
    private boolean includeGermline;
    private boolean includeSomatic;
    private boolean includeUnknownStatus;
    
    public Map<MutationEventType, Boolean> getMutationEventTypes() {
        return mutationEventTypes;
    }

    public void setMutationEventTypes(Map<MutationEventType, Boolean> mutationEventTypes) {
        this.mutationEventTypes = mutationEventTypes;
    }

    public Map<CNA, Boolean> getCopyNumberAlterationEventTypes() {
        return copyNumberAlterationEventTypes;
    }

    public void setCopyNumberAlterationEventTypes(Map<CNA, Boolean> copyNumberAlterationEventTypes) {
        this.copyNumberAlterationEventTypes = copyNumberAlterationEventTypes;
    }

    public Map<String, Boolean> getSelectedTiers() {
        return selectedTiers;
    }

    public void setSelectedTiers(Map<String, Boolean> selectedTiers) {
        this.selectedTiers = selectedTiers;
    }

    public boolean isIncludeDriver() {
        return includeDriver;
    }

    public void setIncludeDriver(boolean includeDriver) {
        this.includeDriver = includeDriver;
    }

    public boolean isIncludeVUS() {
        return includeVUS;
    }

    public void setIncludeVUS(boolean includeVUS) {
        this.includeVUS = includeVUS;
    }

    public boolean isIncludeUnknownOncogenicity() {
        return includeUnknownOncogenicity;
    }

    public void setIncludeUnknownOncogenicity(boolean includeUnknownOncogenicity) {
        this.includeUnknownOncogenicity = includeUnknownOncogenicity;
    }

    public boolean isIncludeUnknownTier() {
        return includeUnknownTier;
    }

    public void setIncludeUnknownTier(boolean includeUnknownTier) {
        this.includeUnknownTier = includeUnknownTier;
    }

    public boolean isIncludeGermline() {
        return includeGermline;
    }

    public void setIncludeGermline(boolean includeGermline) {
        this.includeGermline = includeGermline;
    }

    public boolean isIncludeSomatic() {
        return includeSomatic;
    }

    public void setIncludeSomatic(boolean includeSomatic) {
        this.includeSomatic = includeSomatic;
    }

    public boolean isIncludeUnknownStatus() {
        return includeUnknownStatus;
    }

    public void setIncludeUnknownStatus(boolean includeUnknownStatus) {
        this.includeUnknownStatus = includeUnknownStatus;
    }
}
