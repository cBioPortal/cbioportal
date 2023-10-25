package org.cbioportal.web.parameter;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public class EnrichmentFilter {

    @NotNull
    @Size(min = 1)
    private List<String> alteredIds;
    @NotNull
    @Size(min = 1)
    private List<String> unalteredIds;

    public List<String> getAlteredIds() {
        return alteredIds;
    }

    public void setAlteredIds(List<String> alteredIds) {
        this.alteredIds = alteredIds;
    }

    public List<String> getUnalteredIds() {
        return unalteredIds;
    }

    public void setUnalteredIds(List<String> unalteredIds) {
        this.unalteredIds = unalteredIds;
    }
}
