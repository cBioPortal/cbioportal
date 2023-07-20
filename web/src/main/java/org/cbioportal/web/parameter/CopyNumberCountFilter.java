package org.cbioportal.web.parameter;

public class CopyNumberCountFilter {

    private String molecularProfileId;

    private Integer entrezGeneId;

    public String getMolecularProfileId() {return  molecularProfileId; }
    
    public void setMolecularProfileId(String molecularProfileId) {this.molecularProfileId = molecularProfileId; }

    public Integer getEntrezGeneId() {
        return entrezGeneId;
    }

    public void setEntrezGeneId(Integer entrezGeneId) {
        this.entrezGeneId = entrezGeneId;
    }
}
