package org.cbioportal.model;

import java.io.Serializable;

public class GeneAlias implements Serializable {
    
    private Integer entrezGeneId;
    private String geneAlias;

    public Integer getEntrezGeneId() {
        return entrezGeneId;
    }

    public void setEntrezGeneId(Integer entrezGeneId) {
        this.entrezGeneId = entrezGeneId;
    }

    public String getGeneAlias() {
        return geneAlias;
    }

    public void setGeneAlias(String geneAlias) {
        this.geneAlias = geneAlias;
    }
}
