package org.cbioportal.model;

import org.cbioportal.model.util.Select;

public class AnnotationSourceFilter {
    
    private boolean exludeVus;
    private Select<String> selectedTiers;

    public boolean exludeVus() {
        return exludeVus;
    }

    public void setExludeVus(boolean exludeVus) {
        this.exludeVus = exludeVus;
    }

    public Select<String> getSelectedTiers() {
        return selectedTiers;
    }

    public void setSelectedTiers(Select<String> selectedTiers) {
        this.selectedTiers = selectedTiers;
    }
}
