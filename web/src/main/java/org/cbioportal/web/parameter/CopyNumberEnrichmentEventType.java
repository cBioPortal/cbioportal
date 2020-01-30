package org.cbioportal.web.parameter;

import java.util.Arrays;
import java.util.List;

public enum CopyNumberEnrichmentEventType {
    HOMDEL(-2),
    AMP(2);

    private List<Integer> alterationTypes;

    CopyNumberEnrichmentEventType(Integer... alterationTypes) {
        this.alterationTypes = Arrays.asList(alterationTypes);
    }

    public List<Integer> getAlterationTypes() {
        return alterationTypes;
    }
}
