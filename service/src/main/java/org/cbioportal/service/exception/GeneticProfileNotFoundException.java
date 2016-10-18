package org.cbioportal.service.exception;

public class GeneticProfileNotFoundException extends Exception {

    private String geneticProfileId;

    public GeneticProfileNotFoundException(String geneticProfileId) {
        super();
        this.geneticProfileId = geneticProfileId;
    }

    public String getGeneticProfileId() {
        return geneticProfileId;
    }

    public void setGeneticProfileId(String geneticProfileId) {
        this.geneticProfileId = geneticProfileId;
    }
}
