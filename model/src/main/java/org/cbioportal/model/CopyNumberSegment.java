package org.cbioportal.model;

import java.io.Serializable;

public class CopyNumberSegment implements Serializable {

    private Integer segId;
    private Integer cancerStudyId;
    private Integer sampleId;
    private String chr;
    private Integer start;
    private Integer end;
    private Integer numProbes;
    private Double segmentMean;

    public Integer getSegId() {
        return segId;
    }

    public void setSegId(Integer segId) {
        this.segId = segId;
    }

    public Integer getCancerStudyId() {
        return cancerStudyId;
    }

    public void setCancerStudyId(Integer cancerStudyId) {
        this.cancerStudyId = cancerStudyId;
    }

    public Integer getSampleId() {
        return sampleId;
    }

    public void setSampleId(Integer sampleId) {
        this.sampleId = sampleId;
    }

    public String getChr() {
        return chr;
    }

    public void setChr(String chr) {
        this.chr = chr;
    }

    public Integer getStart() {
        return start;
    }

    public void setStart(Integer start) {
        this.start = start;
    }

    public Integer getEnd() {
        return end;
    }

    public void setEnd(Integer end) {
        this.end = end;
    }

    public Integer getNumProbes() {
        return numProbes;
    }

    public void setNumProbes(Integer numProbes) {
        this.numProbes = numProbes;
    }

    public Double getSegmentMean() {
        return segmentMean;
    }

    public void setSegmentMean(Double segmentMean) {
        this.segmentMean = segmentMean;
    }
}
