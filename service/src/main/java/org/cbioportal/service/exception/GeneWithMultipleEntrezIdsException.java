package org.cbioportal.service.exception;

//TODO: this class should be removed once the issue is resolved database
public class GeneWithMultipleEntrezIdsException extends Exception {

    private String geneId;

    public GeneWithMultipleEntrezIdsException(String geneId) {
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
