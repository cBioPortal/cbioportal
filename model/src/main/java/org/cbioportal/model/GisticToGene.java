package org.cbioportal.model;

import java.io.Serializable;

public class GisticToGene implements Serializable {
    
    private Long gisticRoiId;
    private Integer entrezGeneId;
    private String hugoGeneSymbol;

    public Long getGisticRoiId() {
        return gisticRoiId;
    }

    public void setGisticRoiId(Long gisticRoiId) {
        this.gisticRoiId = gisticRoiId;
    }

    public Integer getEntrezGeneId() {
        return entrezGeneId;
    }

    public void setEntrezGeneId(Integer entrezGeneId) {
        this.entrezGeneId = entrezGeneId;
    }

    public String getHugoGeneSymbol() {
        return hugoGeneSymbol;
    }

    public void setHugoGeneSymbol(String hugoGeneSymbol) {
        this.hugoGeneSymbol = hugoGeneSymbol;
    }
}