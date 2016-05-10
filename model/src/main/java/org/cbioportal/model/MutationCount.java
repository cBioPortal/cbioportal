package org.cbioportal.model;

import java.io.Serializable;

public class MutationCount implements Serializable {

    private Integer geneticProfileId;
    private GeneticProfile geneticProfile;
    private Integer sampleId;
    private Sample sample;
    private Integer mutationCount;

    public Integer getGeneticProfileId() {
        return geneticProfileId;
    }

    public void setGeneticProfileId(Integer geneticProfileId) {
        this.geneticProfileId = geneticProfileId;
    }

    public GeneticProfile getGeneticProfile() {
        return geneticProfile;
    }

    public void setGeneticProfile(GeneticProfile geneticProfile) {
        this.geneticProfile = geneticProfile;
    }

    public Integer getSampleId() {
        return sampleId;
    }

    public void setSampleId(Integer sampleId) {
        this.sampleId = sampleId;
    }

    public Sample getSample() {
        return sample;
    }

    public void setSample(Sample sample) {
        this.sample = sample;
    }

    public Integer getMutationCount() {
        return mutationCount;
    }

    public void setMutationCount(Integer mutationCount) {
        this.mutationCount = mutationCount;
    }
}