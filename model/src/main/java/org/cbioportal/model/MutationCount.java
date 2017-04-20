package org.cbioportal.model;

import java.io.Serializable;

public class MutationCount implements Serializable {

    private String geneticProfileId;
    private String sampleId;
    private Integer mutationCount;

    public String getGeneticProfileId() {
        return geneticProfileId;
    }

    public void setGeneticProfileId(String geneticProfileId) {
        this.geneticProfileId = geneticProfileId;
    }

    public String getSampleId() {
        return sampleId;
    }

    public void setSampleId(String sampleId) {
        this.sampleId = sampleId;
    }

    public Integer getMutationCount() {
        return mutationCount;
    }

    public void setMutationCount(Integer mutationCount) {
        this.mutationCount = mutationCount;
    }
}
