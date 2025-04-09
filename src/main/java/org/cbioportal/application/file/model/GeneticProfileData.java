package org.cbioportal.application.file.model;

import java.util.List;

public class GeneticProfileData {
    private GeneticEntity geneticEntity;
    private Gene gene;
    private List<GeneticEntityProperty> properties;
    private String commaSeparatedValues;

    public String getCommaSeparatedValues() {
        return commaSeparatedValues;
    }

    public List<String> getValues() {
        return List.of(commaSeparatedValues.split(","));
    }

    public void setCommaSeparatedValues(String commaSeparatedValues) {
        this.commaSeparatedValues = commaSeparatedValues;
    }

    public Gene getGene() {
        return gene;
    }

    public void setGene(Gene gene) {
        this.gene = gene;
    }

    public GeneticEntity getGeneticEntity() {
        return geneticEntity;
    }

    public void setGeneticEntity(GeneticEntity geneticEntity) {
        this.geneticEntity = geneticEntity;
    }

    public List<GeneticEntityProperty> getProperties() {
        return properties;
    }

    public void setProperties(List<GeneticEntityProperty> properties) {
        this.properties = properties;
    }
}
