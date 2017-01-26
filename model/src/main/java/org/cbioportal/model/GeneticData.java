package org.cbioportal.model;

import java.io.Serializable;

public class GeneticData implements Serializable {

    private Integer geneticProfileId;
    private String geneticProfileStableId;
    private Integer entrezGeneId;
    private Integer sampleId;
    private String sampleStableId;
    private String value;
    private GeneticProfile geneticProfile;
    private Gene gene;
    private Sample sample;

    public Integer getGeneticProfileId() {
        return geneticProfileId;
    }

    public void setGeneticProfileId(Integer geneticProfileId) {
        this.geneticProfileId = geneticProfileId;
    }

    public String getGeneticProfileStableId() {
        return geneticProfileStableId;
    }

    public void setGeneticProfileStableId(String geneticProfileStableId) {
        this.geneticProfileStableId = geneticProfileStableId;
    }

    public Integer getEntrezGeneId() {
        return entrezGeneId;
    }

    public void setEntrezGeneId(Integer entrezGeneId) {
        this.entrezGeneId = entrezGeneId;
    }

    public Integer getSampleId() {
        return sampleId;
    }

    public void setSampleId(Integer sampleId) {
        this.sampleId = sampleId;
    }

    public String getSampleStableId() {
        return sampleStableId;
    }

    public void setSampleStableId(String sampleStableId) {
        this.sampleStableId = sampleStableId;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public GeneticProfile getGeneticProfile() {
        return geneticProfile;
    }

    public void setGeneticProfile(GeneticProfile geneticProfile) {
        this.geneticProfile = geneticProfile;
    }

    public Gene getGene() {
        return gene;
    }

    public void setGene(Gene gene) {
        this.gene = gene;
    }

    public Sample getSample() {
        return sample;
    }

    public void setSample(Sample sample) {
        this.sample = sample;
    }
}
