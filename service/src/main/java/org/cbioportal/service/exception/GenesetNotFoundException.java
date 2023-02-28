package org.cbioportal.service.exception;

public class GenesetNotFoundException extends Exception {
    
	private String genesetId;

    public GenesetNotFoundException(String genesetId) {
        super();
        this.genesetId = genesetId;
    }

    public String getGenesetId() {
        return genesetId;
    }

    public void setGenesetId(String genesetId) {
        this.genesetId = genesetId;
    }
}
