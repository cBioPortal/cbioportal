package org.cbioportal.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import org.cbioportal.model.util.Select;

import java.io.Serializable;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
public class AlterationFilter extends BaseAlterationFilter implements Serializable {
    
    private Map<MutationEventType, Boolean> mutationBooleanMap;
    private Map<CNA, Boolean> cnaBooleanMap;
    
    @JsonIgnore
    private Select<MutationEventType> mutationTypeSelect;
    @JsonIgnore
    private Select<CNA> cnaTypeSelect;

    // When default constructor is called, the filter is inactive (excludes nothing)
    public AlterationFilter() {}

    public AlterationFilter(Select<MutationEventType> mutationTypesMap,
                            Select<CNA> cnaEventTypes,
                            boolean includeDriver,
                            boolean includeVUS,
                            boolean includeUnknownOncogenicity,
                            boolean includeGermline,
                            boolean includeSomatic,
                            boolean includeUnknownStatus,
                            Select<String> tiersSelect,
                            boolean includeUnknownTier) {
        super(includeDriver, includeVUS, includeUnknownOncogenicity, includeGermline, includeSomatic, includeUnknownStatus, tiersSelect, includeUnknownTier);
        this.mutationTypeSelect = mutationTypesMap;
        this.cnaTypeSelect = cnaEventTypes;
    }

    public void setMutationBooleanMap(Map<MutationEventType, Boolean> selectedTypes) {
        this.mutationBooleanMap = selectedTypes;
        if (selectedTypes == null) {
            this.mutationTypeSelect = Select.all();
        } else {
            this.mutationTypeSelect = Select.byValues(
                selectedTypes.entrySet().stream()
                    .filter(e -> e.getValue())
                    .map(e -> e.getKey()));
            if (selectedTypes.entrySet().stream().allMatch(e -> e.getValue()))
                this.mutationTypeSelect.hasAll(true);
        }
    }

    public Map<MutationEventType, Boolean> getMutationBooleanMap() {
        return mutationBooleanMap;
    }

    @JsonIgnore
    public Select<MutationEventType> getSelectedMutationTypes() {
        if (this.mutationTypeSelect == null)
            return Select.all();
        return this.mutationTypeSelect;
    }

    @JsonIgnore
    public void setSelectedMutationTypes(Select<MutationEventType> typeSelect) {
        this.mutationTypeSelect = typeSelect;
    }
    
    public void setCnaBooleanMap(Map<CNA, Boolean> selectedTypes) {
        this.cnaBooleanMap = selectedTypes;
        if (selectedTypes == null) {
            this.cnaTypeSelect = Select.all();
        } else {
            this.cnaTypeSelect = Select.byValues(
                selectedTypes.entrySet().stream()
                    .filter(e -> e.getValue())
                    .map(e -> e.getKey()));
            if (selectedTypes.entrySet().stream().allMatch(e -> e.getValue()))
                this.cnaTypeSelect.hasAll(true);
        }
    }

    public Map<CNA, Boolean> getCnaBooleanMap() {
        return cnaBooleanMap;
    }

    @JsonIgnore
    public Select<CNA> getSelectedCnaTypes() {
        if (this.cnaTypeSelect == null)
            return Select.all();
        return this.cnaTypeSelect;
    }

    @JsonIgnore
    public void setCnaTypeSelect(Select<CNA> typeSelect) {
        this.cnaTypeSelect = typeSelect;
    }
}
