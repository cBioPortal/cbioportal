package org.cbioportal.web.parameter;

import java.util.Map;

import org.cbioportal.model.CNA;
import org.cbioportal.model.MutationEventType;
import org.cbioportal.model.util.Select;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class AlterationEventTypeFilter {

    private Map<MutationEventType, Boolean> mutationEventTypes;
    private Map<CNA, Boolean> copyNumberAlterationEventTypes;

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

    @JsonIgnore
    public Select<MutationEventType> getMutationTypeSelect() {
        if (mutationEventTypes == null || allOptionsSelected(mutationEventTypes)
                || mutationEventTypes.getOrDefault(MutationEventType.any, false)) {
            return Select.all();
        }

        // if MutationEventType.other is true and not allOptionsSelected
        if (mutationEventTypes.getOrDefault(MutationEventType.other, false)) {
            Select<MutationEventType> select = Select
                    .byValues(mutationEventTypes.entrySet().stream().filter(e -> !e.getValue()).map(e -> e.getKey()));
            // setting this would execute NOT IN clause in sql query
            select.exclude(true);
            return select;
        } else {
            Select<MutationEventType> select = Select
                    .byValues(mutationEventTypes.entrySet().stream().filter(e -> e.getValue()).map(e -> e.getKey()));
            return select;

        }
    }

    @JsonIgnore
    public Select<CNA> getCNAEventTypeSelect() {
        if(allOptionsSelected(copyNumberAlterationEventTypes)) {
            return Select.all();
        }
        return Select.byValues(copyNumberAlterationEventTypes.entrySet().stream().filter(e -> e.getValue())
                .map(e -> e.getKey()));
    }

    @JsonIgnore
    private boolean allOptionsSelected(Map<?, Boolean> options) {
        return options.entrySet().stream().allMatch(e -> e.getValue());
    }

}
