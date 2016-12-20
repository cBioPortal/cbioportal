package org.cbioportal.model;

import java.io.Serializable;

public class GeneticDataValues implements Serializable {

    private Integer geneticProfileId;
    private Integer geneticEntityId;
    private String orderedValuesList;

    public Integer getGeneticProfileId() {
        return geneticProfileId;
    }

    public void setGeneticProfileId(Integer geneticProfileId) {
        this.geneticProfileId = geneticProfileId;
    }

    public Integer getGeneticEntityId() {
        return geneticEntityId;
    }

    public void setGeneticEntityId(Integer geneticEntityId) {
        this.geneticEntityId = geneticEntityId;
    }
    
    public String getOrderedValuesList() {
        return orderedValuesList;
    }

    public void setOrderedValuesList(String orderedValuesList) {
        this.orderedValuesList = orderedValuesList;
    }
}
