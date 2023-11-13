package org.cbioportal.model;

import java.math.BigDecimal;
import jakarta.validation.constraints.NotNull;

public class CopyNumberSeg extends UniqueKeyBase {

    private Long segId;
    private Integer cancerStudyId;
    @NotNull
    private String cancerStudyIdentifier;
    private Integer sampleId;
    @NotNull
    private String sampleStableId;
    @NotNull
    private String patientId;
    @NotNull
    private String chr;
    @NotNull
    private Integer start;
    @NotNull
    private Integer end;
    @NotNull
    private Integer numProbes;
    @NotNull
    private BigDecimal segmentMean;

    public Long getSegId() {
        return segId;
    }

    public void setSegId(Long segId) {
        this.segId = segId;
    }

    public Integer getCancerStudyId() {
        return cancerStudyId;
    }

    public void setCancerStudyId(Integer cancerStudyId) {
        this.cancerStudyId = cancerStudyId;
    }

    public String getCancerStudyIdentifier() {
        return cancerStudyIdentifier;
    }

    public void setCancerStudyIdentifier(String cancerStudyIdentifier) {
        this.cancerStudyIdentifier = cancerStudyIdentifier;
    }

    public Integer getSampleId() {
        return sampleId;
    }

    public void setSampleId(Integer sampleId) {
        this.sampleId = sampleId;
    }

    public String getSampleStableId() {
        return sampleStableId;
    }

    public void setSampleStableId(String sampleStableId) {
        this.sampleStableId = sampleStableId;
    }

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
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

    public BigDecimal getSegmentMean() {
        return segmentMean;
    }

    public void setSegmentMean(BigDecimal segmentMean) {
        this.segmentMean = segmentMean;
    }
}
