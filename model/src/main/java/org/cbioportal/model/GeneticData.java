package org.cbioportal.model;

import org.cbioportal.model.summary.GeneticDataSummary;

public class GeneticData extends GeneticDataSummary {

    private GeneticProfile geneticProfile;
    private Gene gene;
    private Sample sample;

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
