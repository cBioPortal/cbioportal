package org.cbioportal.model;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.cbioportal.model.BaseAlterationFilter;
import org.cbioportal.model.CNA;
import org.cbioportal.model.MutationEventType;
import org.cbioportal.model.util.Select;

import com.fasterxml.jackson.annotation.JsonIgnore;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.*;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(NON_NULL)
public class AlterationFilter extends BaseAlterationFilter implements Serializable {

    private Map<MutationEventType, Boolean> mutationEventTypes;
    private Map<CNA, Boolean> copyNumberAlterationEventTypes;
    private Boolean structuralVariants;

    @JsonIgnore
    private Select<MutationEventType> mutationTypeSelect = Select.all();
    @JsonIgnore
    private Select<CNA> cnaTypeSelect = Select.all();

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
        this.mutationTypeSelect = mutationTypesMap;
        this.cnaTypeSelect = cnaEventTypes;
        this.includeDriver = includeDriver;
        this.includeVUS = includeVUS;
        this.includeUnknownOncogenicity = includeUnknownOncogenicity;
        this.includeGermline = includeGermline;
        this.includeSomatic = includeSomatic;
        this.includeUnknownStatus = includeUnknownStatus;
        this.tiersSelect = tiersSelect;
        this.includeUnknownTier = includeUnknownTier;
    }

    public void setMutationEventTypes(Map<MutationEventType, Boolean> mutationEventTypes) {
        /*
         * Appropriately add fusion mutation type depending on structural variant since fusions are still in mutation table
         * TODO: do necessary changes once fusion data is cleaned up from mutation table
         * */

        if (mutationEventTypes == null || mutationEventTypes.getOrDefault(MutationEventType.any, false)
            || allOptionsSelected(mutationEventTypes, Arrays.asList(MutationEventType.any.toString()))) {
            if(this.structuralVariants == null || this.structuralVariants == true) {
                mutationTypeSelect = Select.all();
            }
        }
        // if MutationEventType.other is true and not allOptionsSelected
        else if (mutationEventTypes.getOrDefault(MutationEventType.other, false)) {
            List<MutationEventType> unSelected = mutationEventTypes
                .entrySet()
                .stream()
                .filter(e -> !e.getValue())
                .map(Entry::getKey)
                .collect(Collectors.toList());
            if(this.structuralVariants == null || this.structuralVariants == false) {
                unSelected.add(MutationEventType.fusion);
            }
            Select<MutationEventType> select = Select.byValues(unSelected);
            // setting this would execute NOT IN clause in sql query
            select.inverse(true);
            mutationTypeSelect = select;
        } else {
            List<MutationEventType> selected = mutationEventTypes
                .entrySet()
                .stream()
                .filter(Entry::getValue)
                .map(Entry::getKey)
                .collect(Collectors.toList());
            if(this.structuralVariants != null && this.structuralVariants == true) {
                selected.add(MutationEventType.fusion);
            }
            mutationTypeSelect = Select.byValues(selected);
        }
        this.mutationEventTypes = mutationEventTypes;
    }

    public Map<MutationEventType, Boolean> getMutationEventTypes() {
        return mutationEventTypes;
    }

    public void setCopyNumberAlterationEventTypes(Map<CNA, Boolean> copyNumberAlterationEventTypes) {
        if (allOptionsSelected(copyNumberAlterationEventTypes, null)) {
            cnaTypeSelect = Select.all();
        } else {
            cnaTypeSelect = Select.byValues(
                copyNumberAlterationEventTypes.entrySet().stream().filter(Entry::getValue).map(Entry::getKey));
        }
        this.copyNumberAlterationEventTypes = copyNumberAlterationEventTypes;
    }

    public Map<CNA, Boolean> getCopyNumberAlterationEventTypes() {
        return copyNumberAlterationEventTypes;
    }

    public Boolean getStructuralVariants() {
        return structuralVariants;
    }

    public void setStructuralVariants(Boolean structuralVariants) {
        this.structuralVariants = structuralVariants;
    }

    @JsonIgnore
    public Select<MutationEventType> getMutationTypeSelect() {
        return mutationTypeSelect;
    }

    @JsonIgnore
    public void setMutationTypeSelect(Select<MutationEventType> mutationTypeSelect) {
        this.mutationTypeSelect = mutationTypeSelect;
    }

    @JsonIgnore
    public Select<CNA> getCNAEventTypeSelect() {
        return cnaTypeSelect;
    }

    @JsonIgnore
    public void setCnaTypeSelect(Select<CNA> cnaTypeSelect) {
        this.cnaTypeSelect = cnaTypeSelect;
    }

    @JsonIgnore
    private boolean allOptionsSelected(Map<?, Boolean> options, List<String> excludeKeys) {
        return options.entrySet().stream().allMatch(e -> {
            return excludeKeys == null || !excludeKeys.contains(e.getKey().toString()) ? e.getValue() : true;
        });
    }

}
