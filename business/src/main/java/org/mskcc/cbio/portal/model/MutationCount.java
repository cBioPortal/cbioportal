package org.mskcc.cbio.portal.model;

import java.io.Serializable;
import org.cbioportal.model.MolecularProfile;
import org.cbioportal.model.Sample;

public class MutationCount implements Serializable {
    private Integer geneticProfileId;
    private MolecularProfile geneticProfile;
    private Integer sampleId;
    private Sample sample;
    private Integer mutationCount;

    public Integer getGeneticProfileId() {
        return geneticProfileId;
    }

    public void setGeneticProfileId(Integer geneticProfileId) {
        this.geneticProfileId = geneticProfileId;
    }

    public MolecularProfile getGeneticProfile() {
        return geneticProfile;
    }

    public void setGeneticProfile(MolecularProfile geneticProfile) {
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
