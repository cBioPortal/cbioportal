package org.cbioportal.model;

import java.math.BigDecimal;

public class CopyNumberSeg extends UniqueKeyBase {

    private Integer segId;
    private Integer cancerStudyId;
    private String molecularProfileId;
    private String cancerStudyIdentifier;
    private Integer sampleId;
    private String sampleStableId;
    private String patientId;
    private String chr;
    private Integer start;
    private Integer end;
    private Integer numProbes;
    private BigDecimal segmentMean;

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
    
    public String getMolecularProfileId() {
        return molecularProfileId;
    }

    public void setMolecularProfileId(String molecularProfileId) {
        this.molecularProfileId = molecularProfileId;
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
