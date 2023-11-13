package org.cbioportal.service.exception;

public class GeneNotFoundException extends Exception {

    private String geneId;

    public GeneNotFoundException(String geneId) {
        super();
        this.geneId = geneId;
    }

    public String getGeneId() {
        return geneId;
    }

    public void setGeneId(String geneId) {
        this.geneId = geneId;
    }
}
