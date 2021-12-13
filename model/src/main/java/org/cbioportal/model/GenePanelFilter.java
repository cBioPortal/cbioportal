package org.cbioportal.model;

import java.io.Serializable;

public class GenePanelFilter implements Serializable {
    private String genePanelId;
    private String molecularProfileSuffix;

    public String getGenePanelId() {
        return genePanelId;
    }

    public void setGenePanelId(String genePanelId) {
        this.genePanelId = genePanelId;
    }

    public String getMolecularProfileSuffix() {
        return molecularProfileSuffix;
    }

    public void setMolecularProfileSuffix(String molecularProfileSuffix) {
        this.molecularProfileSuffix = molecularProfileSuffix;
    }
}
