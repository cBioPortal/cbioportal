package org.cbioportal.model;

import java.io.Serializable;

public class CopyNumberCount implements Serializable {
    
    private String geneticProfileId;
    private Integer entrezGeneId;
    private Integer alteration;
    private Integer numberOfSamples;
    private Integer numberOfSamplesWithAlterationInGene;

    public String getGeneticProfileId() {
        return geneticProfileId;
    }

    public void setGeneticProfileId(String geneticProfileId) {
        this.geneticProfileId = geneticProfileId;
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
