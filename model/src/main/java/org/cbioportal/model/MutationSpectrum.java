package org.cbioportal.model;

import java.io.Serializable;

public class MutationSpectrum implements Serializable {
    
    private String geneticProfileId;
    private String sampleId;
    private Integer ctoA;
    private Integer ctoG;
    private Integer ctoT;
    private Integer ttoA;
    private Integer ttoC;
    private Integer ttoG;

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

    public Integer getCtoA() {
        return ctoA;
    }

    public void setCtoA(Integer ctoA) {
        this.ctoA = ctoA;
    }

    public Integer getCtoG() {
        return ctoG;
    }

    public void setCtoG(Integer ctoG) {
        this.ctoG = ctoG;
    }

    public Integer getCtoT() {
        return ctoT;
    }

    public void setCtoT(Integer ctoT) {
        this.ctoT = ctoT;
    }

    public Integer getTtoA() {
        return ttoA;
    }

    public void setTtoA(Integer ttoA) {
        this.ttoA = ttoA;
    }

    public Integer getTtoC() {
        return ttoC;
    }

    public void setTtoC(Integer ttoC) {
        this.ttoC = ttoC;
    }

    public Integer getTtoG() {
        return ttoG;
    }

    public void setTtoG(Integer ttoG) {
        this.ttoG = ttoG;
    }
}
