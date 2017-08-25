package org.cbioportal.model;

import java.io.Serializable;
import java.util.List;

public class GenePanelData implements Serializable {
    
    private String molecularProfileId;
    private String sampleId;
    private String genePanelId;
    private List<Integer> entrezGeneIds;

    public String getMolecularProfileId() {
        return molecularProfileId;
    }

    public void setMolecularProfileId(String molecularProfileId) {
        this.molecularProfileId = molecularProfileId;
    }

    public String getSampleId() {
        return sampleId;
    }

    public void setSampleId(String sampleId) {
        this.sampleId = sampleId;
    }

    public String getGenePanelId() {
        return genePanelId;
    }

    public void setGenePanelId(String genePanelId) {
        this.genePanelId = genePanelId;
    }

    public List<Integer> getEntrezGeneIds() {
        return entrezGeneIds;
    }

    public void setEntrezGeneIds(List<Integer> entrezGeneIds) {
        this.entrezGeneIds = entrezGeneIds;
    }
}
