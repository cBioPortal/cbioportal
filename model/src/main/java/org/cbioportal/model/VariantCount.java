package org.cbioportal.model;

import java.io.Serializable;

public class VariantCount implements Serializable {
    
    private String geneticProfileId;
    private Integer entrezGeneId;
    private String keyword;
    private Integer numberOfSamples;
    private Integer numberOfSamplesWithMutationInGene;
    private Integer numberOfSamplesWithKeyword;

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

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public Integer getNumberOfSamples() {
        return numberOfSamples;
    }

    public void setNumberOfSamples(Integer numberOfSamples) {
        this.numberOfSamples = numberOfSamples;
    }

    public Integer getNumberOfSamplesWithMutationInGene() {
        return numberOfSamplesWithMutationInGene;
    }

    public void setNumberOfSamplesWithMutationInGene(Integer numberOfSamplesWithMutationInGene) {
        this.numberOfSamplesWithMutationInGene = numberOfSamplesWithMutationInGene;
    }

    public Integer getNumberOfSamplesWithKeyword() {
        return numberOfSamplesWithKeyword;
    }

    public void setNumberOfSamplesWithKeyword(Integer numberOfSamplesWithKeyword) {
        this.numberOfSamplesWithKeyword = numberOfSamplesWithKeyword;
    }
}
