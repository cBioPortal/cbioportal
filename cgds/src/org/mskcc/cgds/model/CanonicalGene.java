package org.mskcc.cgds.model;

public class CanonicalGene extends Gene {
    private long entrezGeneId;
    private String hugoGeneSymbol;
    private double somaticMutationFrequency;

    public CanonicalGene(long entrezGeneId, String hugoGeneSymbol) {
        this.entrezGeneId = entrezGeneId;
        this.hugoGeneSymbol = hugoGeneSymbol;
    }

    public long getEntrezGeneId() {
        return entrezGeneId;
    }

    public void setEntrezGeneId(int entrezGeneId) {
        this.entrezGeneId = entrezGeneId;
    }

    public String getHugoGeneSymbol() {
        return hugoGeneSymbol;
    }

    public String getStandardSymbol() {
        return hugoGeneSymbol;
    }

    public void setHugoGeneSymbol(String hugoGeneSymbol) {
        this.hugoGeneSymbol = hugoGeneSymbol;
    }

    public String toString() {
        return this.getHugoGeneSymbol();
    }

    public boolean equals(Object obj0) {
        CanonicalGene gene0 = (CanonicalGene) obj0;
        if (gene0.getEntrezGeneId() == entrezGeneId) {
            return true;
        }
        return false;
    }

    public double getSomaticMutationFrequency() {
        return somaticMutationFrequency;
    }

    public void setSomaticMutationFrequency(double somaticMutationFrequency) {
        this.somaticMutationFrequency = somaticMutationFrequency;
    }

    public int hashCode() {
        return (int) entrezGeneId;
    }
}