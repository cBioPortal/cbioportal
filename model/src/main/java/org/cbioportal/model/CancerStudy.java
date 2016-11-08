package org.cbioportal.model;

import org.cbioportal.model.summary.CancerStudySummary;


public class CancerStudy extends CancerStudySummary {

    private TypeOfCancer typeOfCancer;
    private Integer sampleCount;
    private String stableID;
    private Integer allCount;
    private Integer sequencedCount;
    private Integer cnaCount;
    private Integer rna_seq_v2_mrna_count;
    private Integer mrna_count;
    private Integer microrna_count;
    private Integer methylation_hm27_count;
    private Integer rppa_count;
    private Integer complete_count;
    
    public Integer getAllCount() {
        return allCount;
    }

    public void setAllCount(Integer allCount) {
        this.allCount = allCount;
    }

    public Integer getSequencedCount() {
        return sequencedCount;
    }

    public void setSequencedCount(Integer sequencedCount) {
        this.sequencedCount = sequencedCount;
    }

    public Integer getCnaCount() {
        return cnaCount;
    }

    public void setCnaCount(Integer cnaCount) {
        this.cnaCount = cnaCount;
    }

    public Integer getRna_seq_v2_mrna_count() {
        return rna_seq_v2_mrna_count;
    }

    public void setRna_seq_v2_mrna_count(Integer rna_seq_v2_mrna_count) {
        this.rna_seq_v2_mrna_count = rna_seq_v2_mrna_count;
    }

    public Integer getMrna_count() {
        return mrna_count;
    }

    public void setMrna_count(Integer mrna_count) {
        this.mrna_count = mrna_count;
    }

    public Integer getMicrorna_count() {
        return microrna_count;
    }

    public void setMicrorna_count(Integer microrna_count) {
        this.microrna_count = microrna_count;
    }

    public Integer getMethylation_hm27_count() {
        return methylation_hm27_count;
    }

    public void setMethylation_hm27_count(Integer methylation_hm27_count) {
        this.methylation_hm27_count = methylation_hm27_count;
    }

    public Integer getRppa_count() {
        return rppa_count;
    }

    public void setRppa_count(Integer rppa_count) {
        this.rppa_count = rppa_count;
    }

    public Integer getComplete_count() {
        return complete_count;
    }

    public void setComplete_count(Integer complete_count) {
        this.complete_count = complete_count;
    }

    public String getStableID() {
        return stableID;
    }

    public void setStableID(String stableID) {
        this.stableID = stableID;
    }

    public TypeOfCancer getTypeOfCancer() {
        return typeOfCancer;
    }

    public void setTypeOfCancer(TypeOfCancer typeOfCancer) {
        this.typeOfCancer = typeOfCancer;
    }

    public Integer getSampleCount() {
        return sampleCount;
    }

    public void setSampleCount(Integer sampleCount) {
        this.sampleCount = sampleCount;
    }
}