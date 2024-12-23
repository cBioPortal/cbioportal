package org.cbioportal.model;

import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

public class SampleToPanel implements Serializable {
    
    
    @NotNull
    private String sampleUniqueId;
    @NotNull
    private String genePanelId;
    private String geneticProfileId;

    public String getSampleUniqueId() {
        return sampleUniqueId;
    }

    public void setSampleUniqueId(String sampleUniqueId) {
        this.sampleUniqueId = sampleUniqueId;
    }

    public String getGenePanelId() {
        return genePanelId;
    }

    public void setGenePanelId(String getPanelId) {
        this.genePanelId = genePanelId;
    }

    public String getGeneticProfileId() {
        return geneticProfileId;
    }

    public void setGeneticProfileId(String geneticProfileId) {
        this.geneticProfileId = geneticProfileId;
    }

    
}
