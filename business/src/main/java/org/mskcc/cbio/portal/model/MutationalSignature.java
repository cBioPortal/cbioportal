package org.mskcc.cbio.portal.model;

import java.io.Serializable;
import java.util.Map;

public class MutationalSignature implements Serializable {
    private String sample;
    private int[] counts;
    private String[] mutationTypes;

    public MutationalSignature(
        String[] mutationTypes,
        String sample,
        int[] counts
    ) {
        this.sample = sample;
        this.counts = counts;
        this.mutationTypes = mutationTypes;
    }

    public String getSample() {
        return sample;
    }

    public void setSample(String sample) {
        this.sample = sample;
    }

    public int[] getCounts() {
        return counts;
    }

    public void setCounts(int[] counts) {
        this.counts = counts;
    }

    public String[] getMutationTypes() {
        return mutationTypes;
    }

    public void setMutationTypes(String[] mutationTypes) {
        this.mutationTypes = mutationTypes;
    }
}
