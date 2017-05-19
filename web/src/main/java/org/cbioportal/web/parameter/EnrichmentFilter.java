package org.cbioportal.web.parameter;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

public class EnrichmentFilter {
    
    @NotNull
    @Size(min = 1)
    private List<String> alteredSampleIds;
    @NotNull
    @Size(min = 1)
    private List<String> unalteredSampleIds;;

    public List<String> getAlteredSampleIds() {
        return alteredSampleIds;
    }

    public void setAlteredSampleIds(List<String> alteredSampleIds) {
        this.alteredSampleIds = alteredSampleIds;
    }

    public List<String> getUnalteredSampleIds() {
        return unalteredSampleIds;
    }

    public void setUnalteredSampleIds(List<String> unalteredSampleIds) {
        this.unalteredSampleIds = unalteredSampleIds;
    }
}
