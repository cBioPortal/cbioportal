package org.cbioportal.model;

import java.io.Serializable;
import jakarta.validation.constraints.NotNull;

public class CopyNumberCount implements Serializable {
    
    @NotNull
    private String molecularProfileId;
    @NotNull
    private Integer entrezGeneId;
    @NotNull
    private Integer alteration;
    @NotNull
    private Integer numberOfSamples;
    @NotNull
    private Integer numberOfSamplesWithAlterationInGene;

    public String getMolecularProfileId() {
        return molecularProfileId;
    }

    public void setMolecularProfileId(String molecularProfileId) {
        this.molecularProfileId = molecularProfileId;
    }

    public Integer getEntrezGeneId() {
        return entrezGeneId;
    }

    public void setEntrezGeneId(Integer entrezGeneId) {
        this.entrezGeneId = entrezGeneId;
    }

    public Integer getAlteration() {
        return alteration;
    }

    public void setAlteration(Integer alteration) {
        this.alteration = alteration;
    }

    public Integer getNumberOfSamples() {
        return numberOfSamples;
    }

    public void setNumberOfSamples(Integer numberOfSamples) {
        this.numberOfSamples = numberOfSamples;
    }

    public Integer getNumberOfSamplesWithAlterationInGene() {
        return numberOfSamplesWithAlterationInGene;
    }

    public void setNumberOfSamplesWithAlterationInGene(Integer numberOfSamplesWithAlterationInGene) {
        this.numberOfSamplesWithAlterationInGene = numberOfSamplesWithAlterationInGene;
    }
}
