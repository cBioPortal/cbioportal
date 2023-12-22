package org.cbioportal.web.parameter;

import java.util.Arrays;
import java.util.List;

public enum DiscreteCopyNumberEventType {

    HOMDEL_AND_AMP(-2, 2),
    HOMDEL(-2),
    AMP(2),
    GAIN(1),
    HETLOSS(-1),
    DIPLOID(0),
    ALL(-2, -1, 0, 1, 2);

    private List<Integer> alterationTypes;

    DiscreteCopyNumberEventType(Integer... alterationTypes) {
        this.alterationTypes = Arrays.asList(alterationTypes);
    }

    public List<Integer> getAlterationTypes() {
        return alterationTypes;
    }
}
