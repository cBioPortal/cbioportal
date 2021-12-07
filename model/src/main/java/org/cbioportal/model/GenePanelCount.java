package org.cbioportal.model;

import java.io.Serializable;
import java.util.List;

public class GenePanelCount implements Serializable {
    private String genePanelId;
    private List<String> molecularProfileIds;
    private int count;
    
    public String getGenePanelId() {
        return genePanelId;
    }

    public void setGenePanelId(String genePanelId) {
        this.genePanelId = genePanelId;
    }

    public List<String> getMolecularProfileIds() {
        return molecularProfileIds;
    }

    public void setMolecularProfileIds(List<String> molecularProfileIds) {
        this.molecularProfileIds = molecularProfileIds;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
