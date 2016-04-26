package org.cbioportal.model;

public class MutationCount {
    private GeneticProfile geneticProfile;

    private Sample sample;

    private Integer mutationCount;

    public GeneticProfile getGeneticProfile() {
        return geneticProfile;
    }

    public void setGeneticProfile(GeneticProfile geneticProfile) {
        this.geneticProfile = geneticProfile;
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