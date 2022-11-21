package org.cbioportal.model;

public class AlterationCountByStructuralVariant extends AlterationCountBase {

    private Integer gene1EntrezGeneId;
    private String gene1HugoGeneSymbol; 
    private Integer gene2EntrezGeneId;
    private String gene2HugoGeneSymbol;
    
    public Integer getGene1EntrezGeneId() {
        return gene1EntrezGeneId;
    }

    public void setGene1EntrezGeneId(Integer gene1EntrezGeneId) {
        this.gene1EntrezGeneId = gene1EntrezGeneId;
    }

    public String getGene1HugoGeneSymbol() {
        return gene1HugoGeneSymbol;
    }

    public void setGene1HugoGeneSymbol(String gene1HugoGeneSymbol) {
        this.gene1HugoGeneSymbol = gene1HugoGeneSymbol;
    }

    public Integer getGene2EntrezGeneId() {
        return gene2EntrezGeneId;
    }

    public void setGene2EntrezGeneId(Integer gene2EntrezGeneId) {
        this.gene2EntrezGeneId = gene2EntrezGeneId;
    }

    public String getGene2HugoGeneSymbol() {
        return gene2HugoGeneSymbol;
    }

    public void setGene2HugoGeneSymbol(String gene2HugoGeneSymbol) {
        this.gene2HugoGeneSymbol = gene2HugoGeneSymbol;
    }
    
}
