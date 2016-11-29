package org.cbioportal.model;

import java.io.Serializable;

public class GeneticDataSamples implements Serializable {

    private Integer geneticProfileId;
    private String orderedSamplesList;

    public Integer getGeneticProfileId() {
        return geneticProfileId;
    }

    public void setGeneticProfileId(Integer geneticProfileId) {
        this.geneticProfileId = geneticProfileId;
    }

    public String getOrderedSamplesList() {
    	//TODO do we want to return as List<> here?
        return orderedSamplesList;
    }

    public void setOrderedSamplesList(String orderedSamplesList) {
        this.orderedSamplesList = orderedSamplesList;
    }
}
