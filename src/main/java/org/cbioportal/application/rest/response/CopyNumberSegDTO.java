package org.cbioportal.application.rest.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(name = "CopyNumberSeg", description = "Represents a copy number segment")
public class CopyNumberSegDTO {
  private String uniqueSampleKey;
  private String uniquePatientKey;
  private String studyId;
  private String sampleId;
  private String patientId;
  private String chromosome;
  private Integer start;
  private Integer end;
  private Integer numberOfProbes;
  private BigDecimal segmentMean;

  public String getStudyId() {
    return studyId;
  }

  public String getUniqueSampleKey() {
    return uniqueSampleKey;
  }

  public void setUniqueSampleKey(String uniqueSampleKey) {
    this.uniqueSampleKey = uniqueSampleKey;
  }

  public String getUniquePatientKey() {
    return uniquePatientKey;
  }

  public void setUniquePatientKey(String uniquePatientKey) {
    this.uniquePatientKey = uniquePatientKey;
  }

  public void setStudyId(String studyId) {
    this.studyId = studyId;
  }

  public String getSampleId() {
    return sampleId;
  }

  public void setSampleId(String sampleId) {
    this.sampleId = sampleId;
  }

  public String getPatientId() {
    return patientId;
  }

  public void setPatientId(String patientId) {
    this.patientId = patientId;
  }

  public String getChromosome() {
    return chromosome;
  }

  public void setChromosome(String chromosome) {
    this.chromosome = chromosome;
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

  public Integer getNumberOfProbes() {
    return numberOfProbes;
  }

  public void setNumberOfProbes(Integer numberOfProbes) {
    this.numberOfProbes = numberOfProbes;
  }

  public BigDecimal getSegmentMean() {
    return segmentMean;
  }

  public void setSegmentMean(BigDecimal segmentMean) {
    this.segmentMean = segmentMean;
  }
}
