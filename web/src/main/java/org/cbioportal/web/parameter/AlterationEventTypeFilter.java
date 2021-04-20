package org.cbioportal.web.parameter;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.cbioportal.model.CNA;
import org.cbioportal.model.MutationEventType;
import org.cbioportal.model.util.Select;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class AlterationEventTypeFilter {

    private Map<MutationEventType, Boolean> mutationEventTypes;
    private Map<CNA, Boolean> copyNumberAlterationEventTypes;
    private Boolean structuralVariants;

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

    public Boolean getStructuralVariants() {
        return structuralVariants;
    }

    public void setStructuralVariants(Boolean structuralVariants) {
        this.structuralVariants = structuralVariants;
    }

    @JsonIgnore
    public Select<MutationEventType> getMutationTypeSelect() {
        /*
        * Appropriately add fusion mutation type depending on structural variant since fusions are still in mutation table
        * TODO: do necessary changes once fusion data is cleaned up from mutation table
        * */
        
        if (mutationEventTypes == null || mutationEventTypes.getOrDefault(MutationEventType.any, false)
                || allOptionsSelected(mutationEventTypes, Arrays.asList(MutationEventType.any.toString()))) {
            if(this.structuralVariants == null || this.structuralVariants == true) {
                return Select.all();
            }
        }

        // if MutationEventType.other is true and not allOptionsSelected
        if (mutationEventTypes.getOrDefault(MutationEventType.other, false)) {
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
            return select;
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
            return Select.byValues(selected);
        }
    }

    @JsonIgnore
    public Select<CNA> getCNAEventTypeSelect() {
        if (allOptionsSelected(copyNumberAlterationEventTypes, null)) {
            return Select.all();
        }
        return Select.byValues(
                copyNumberAlterationEventTypes.entrySet().stream().filter(Entry::getValue).map(Entry::getKey));
    }

    @JsonIgnore
    private boolean allOptionsSelected(Map<?, Boolean> options, List<String> excludeKeys) {
        return options.entrySet().stream().allMatch(e -> {
            return excludeKeys == null || !excludeKeys.contains(e.getKey().toString()) ? e.getValue() : true;
        });
    }

}
