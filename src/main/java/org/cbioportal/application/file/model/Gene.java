package org.cbioportal.application.file.model;

public class Gene {
    private String hugoGeneSymbol;
    private Integer entrezGeneId;
    private String type;

    public String getHugoGeneSymbol() {
        return hugoGeneSymbol;
    }

    public void setHugoGeneSymbol(String hugoGeneSymbol) {
        this.hugoGeneSymbol = hugoGeneSymbol;
    }

    public Integer getEntrezGeneId() {
        return entrezGeneId;
    }

    public void setEntrezGeneId(Integer entrezGeneId) {
        this.entrezGeneId = entrezGeneId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
