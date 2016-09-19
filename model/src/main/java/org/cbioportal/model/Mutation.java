package org.cbioportal.model;

import org.cbioportal.model.summary.MutationSummary;

public class Mutation extends MutationSummary {

    private MutationEvent mutationEvent;
    private GeneticProfile geneticProfile;
    private Sample sample;
    private Gene gene;

    public MutationEvent getMutationEvent() {
        return mutationEvent;
    }

    public void setMutationEvent(MutationEvent mutationEvent) {
        this.mutationEvent = mutationEvent;
    }

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

    public Gene getGene() {
        return gene;
    }

    public void setGene(Gene gene) {
        this.gene = gene;
    }
}