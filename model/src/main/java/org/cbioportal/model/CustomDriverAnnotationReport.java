package org.cbioportal.model;

import java.io.Serializable;
import java.util.Set;

public class CustomDriverAnnotationReport implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private final boolean hasBinary;
    private final Set<String> tiers;
    
    public CustomDriverAnnotationReport(boolean hasBinary, Set<String> tiers) {
        this.hasBinary = hasBinary;
        this.tiers = tiers;
    }
    
    public boolean getHasBinary() {
        return hasBinary;
    }

    public Set<String> getTiers() {
        return tiers;
    }
}