package org.cbioportal.model;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.cbioportal.model.util.Select;

import com.fasterxml.jackson.annotation.JsonIgnore;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.*;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(NON_NULL)
public class AlterationFilter extends BaseAlterationFilter implements Serializable {

    private Map<MutationEventType, Boolean> mutationEventTypes = new HashMap<>();
    private Map<CNA, Boolean> copyNumberAlterationEventTypes = new HashMap<>();
    private Boolean structuralVariants;

    @JsonIgnore
    private Select<MutationEventType> mutationTypeSelect;
    @JsonIgnore
    private Select<CNA> cnaTypeSelect;

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

    public Map<MutationEventType, Boolean> getMutationEventTypes() {
        return mutationEventTypes;
    }

    public void setMutationEventTypes(Map<MutationEventType, Boolean> mutationEventTypes) {
        if (mutationEventTypes == null) {
            throw new IllegalArgumentException("null value is not allowed for mutationEventTypes");
        }
        this.mutationEventTypes = mutationEventTypes;
    }

    public Map<CNA, Boolean> getCopyNumberAlterationEventTypes() {
        return copyNumberAlterationEventTypes;
    }

    public void setCopyNumberAlterationEventTypes(Map<CNA, Boolean> copyNumberAlterationEventTypes) {
        if (copyNumberAlterationEventTypes == null) {
            throw new IllegalArgumentException("null value is not allowed for copyNumberAlterationEventTypes");
        }
        this.copyNumberAlterationEventTypes = copyNumberAlterationEventTypes;
    }

    public Boolean getStructuralVariants() {
        return structuralVariants;
    }

    public void setStructuralVariants(Boolean structuralVariants) {
        this.structuralVariants = structuralVariants;
    }

    @JsonIgnore
    public Select<MutationEventType> getMutationTypeSelect() {
        if (mutationTypeSelect != null)
            return mutationTypeSelect;

        if (mutationEventTypes == null || mutationEventTypes.getOrDefault(MutationEventType.any, false)
            || allOptionsSelected(mutationEventTypes, Arrays.asList(MutationEventType.any.toString()))) {
            return Select.all();
        }

        // if MutationEventType.other is true and not allOptionsSelected
        if (mutationEventTypes.getOrDefault(MutationEventType.other, false)) {
            List<MutationEventType> unSelected = mutationEventTypes
                .entrySet()
                .stream()
                .filter(e -> !e.getValue())
                .map(Entry::getKey)
                .collect(Collectors.toList());
            Select<MutationEventType> select = Select.byValues(unSelected);
            // setting this would execute NOT IN clause in sql query
            select.inverse(true);
            return select;
        } else {
            List<MutationEventType> selected = mutationEventTypes
                .entrySet()
                .stream()
                .filter(Entry::getValue)
                .map(Entry::getKey)
                .collect(Collectors.toList());
            return Select.byValues(selected);
        }
    }

    @JsonIgnore
    public Select<CNA> getCNAEventTypeSelect() {
        if (cnaTypeSelect != null)
            return cnaTypeSelect;
        if (allOptionsSelected(copyNumberAlterationEventTypes, null)) {
            return Select.all();
        }
        return Select.byValues(
            copyNumberAlterationEventTypes.entrySet().stream().filter(Entry::getValue).map(Entry::getKey));
    }

    @JsonIgnore
    public void setMutationTypeSelect(Select<MutationEventType> mutationTypeSelect) {
        this.mutationTypeSelect = mutationTypeSelect;
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
