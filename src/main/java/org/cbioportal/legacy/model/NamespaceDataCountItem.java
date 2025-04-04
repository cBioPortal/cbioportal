package org.cbioportal.legacy.model;

import java.util.List;

public class NamespaceDataCountItem extends NamespaceAttribute {

    private List<NamespaceDataCount> counts;

    public List<NamespaceDataCount> getCounts() {
        return counts;
    }

    public void setCounts(List<NamespaceDataCount> counts) {
        this.counts = counts;
    }
}
