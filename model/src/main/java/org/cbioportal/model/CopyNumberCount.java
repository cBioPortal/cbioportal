package org.cbioportal.model;

import java.io.Serializable;

public class CopyNumberCount implements Serializable {
    
    private String molecularProfileId;
    private Integer entrezGeneId;
    private Integer alteration;
    private Integer numberOfSamples;
    private Integer numberOfSamplesWithAlterationInGene;

    public String getMolecularProfileId() {
        return molecularProfileId;
    }

    public void setMolecularProfileId(String molecularProfileId) {
        this.molecularProfileId = molecularProfileId;
    }

    public Integer getEntrezGeneId() {
        return entrezGeneId;
    }

    public void setEntrezGeneId(Integer entrezGeneId) {
        this.entrezGeneId = entrezGeneId;
    }

    public Integer getAlteration() {
        return alteration;
    }

    public void setAlteration(Integer alteration) {
        this.alteration = alteration;
    }

    public Integer getNumberOfSamples() {
        return numberOfSamples;
    }

    public void setNumberOfSamples(Integer numberOfSamples) {
        this.numberOfSamples = numberOfSamples;
    }

    public Integer getNumberOfSamplesWithAlterationInGene() {
        return numberOfSamplesWithAlterationInGene;
    }

    public void setNumberOfSamplesWithAlterationInGene(Integer numberOfSamplesWithAlterationInGene) {
        this.numberOfSamplesWithAlterationInGene = numberOfSamplesWithAlterationInGene;
    }
}
