package org.cbioportal.model;

import java.util.List;

public class GenePanelFilter {
    private String genePanel;
    private List<String> molecularProfileIds;

    public String getGenePanel() {
        return genePanel;
    }

    public void setGenePanel(String genePanel) {
        this.genePanel = genePanel;
    }

    public List<String> getMolecularProfileIds() {
        return molecularProfileIds;
    }

    public void setMolecularProfileIds(List<String> molecularProfileIds) {
        this.molecularProfileIds = molecularProfileIds;
    }
}
