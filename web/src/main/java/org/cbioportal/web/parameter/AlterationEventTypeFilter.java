package org.cbioportal.web.parameter;

import org.cbioportal.model.CNA;
import org.cbioportal.model.MutationEventType;

import java.util.Map;

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
    
}
