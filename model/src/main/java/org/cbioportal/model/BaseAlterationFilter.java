package org.cbioportal.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import org.cbioportal.model.util.Select;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
public class BaseAlterationFilter implements Serializable {

    private boolean includeGermline = true;
    private boolean includeSomatic = true;
    private boolean includeUnknownStatus = true;
    private boolean includeDriver = true;
    private boolean includeVUS = true;
    private boolean includeUnknownOncogenicity = true;
    private Map<String, Boolean> tiersBooleanMap = new HashMap<>();
    private boolean includeUnknownTier = true;

    @JsonIgnore
    private Select<String> tiersSelect;

    // When default constructor is called, the filter is inactive (excludes nothing)
    public BaseAlterationFilter() {
    }

    public BaseAlterationFilter(boolean includeDriver,
                                boolean includeVUS,
                                boolean includeUnknownOncogenicity,
                                boolean includeGermline,
                                boolean includeSomatic,
                                boolean includeUnknownStatus,
                                Select<String> tiersSelect,
                                boolean includeUnknownTier) {
        this.includeGermline = includeGermline;
        this.includeSomatic = includeSomatic;
        this.includeUnknownStatus = includeUnknownStatus;
        this.includeDriver = includeDriver;
        this.includeVUS = includeVUS;
        this.includeUnknownOncogenicity = includeUnknownOncogenicity;
        this.tiersSelect = tiersSelect;
        this.includeUnknownTier = includeUnknownTier;
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

    public boolean getIncludeUnknownTier() {
        return includeUnknownTier;
    }

    public void setIncludeUnknownTier(boolean includeUnknownTier) {
        this.includeUnknownTier = includeUnknownTier;
    }

    public void setTiersBooleanMap(Map<String, Boolean> tiersBooleanMap) {
        if (tiersBooleanMap == null) {
            this.tiersSelect = Select.all();
        } else {
            this.tiersSelect = Select.byValues(
                tiersBooleanMap.entrySet().stream()
                    .filter(e -> e.getValue())
                    .map(e -> e.getKey()));
            if (tiersBooleanMap.size() > 0 && tiersBooleanMap.entrySet().stream().allMatch(e -> e.getValue()))
                this.tiersSelect.hasAll(true);
        }
    }

    public Map<String, Boolean> getTiersBooleanMap() {
        return tiersBooleanMap;
    }
    
    @JsonIgnore
    public Select<String> getSelectedTiers() {
        if (tiersSelect == null)
            return Select.all();
        return tiersSelect;
    }

    @JsonIgnore
    public void setSelectedTiers(Select<String> tiersSelect) {
        this.tiersSelect = tiersSelect;
    }

}
