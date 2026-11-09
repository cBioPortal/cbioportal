package org.cbioportal.application.rest.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(
    name = "CopyNumberSeg",
    description =
        "Represents a copy number segment. The allele-specific fields (tcn, lcn, cellularFraction,"
            + " purity, ploidy, dipLogR) are only returned with the DETAILED projection.")
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

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Integer tcn;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Integer lcn;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private BigDecimal cellularFraction;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private BigDecimal purity;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private BigDecimal ploidy;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private BigDecimal dipLogR;

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

  public Integer getTcn() {
    return tcn;
  }

  public void setTcn(Integer tcn) {
    this.tcn = tcn;
  }

  public Integer getLcn() {
    return lcn;
  }

  public void setLcn(Integer lcn) {
    this.lcn = lcn;
  }

  public BigDecimal getCellularFraction() {
    return cellularFraction;
  }

  public void setCellularFraction(BigDecimal cellularFraction) {
    this.cellularFraction = cellularFraction;
  }

  public BigDecimal getPurity() {
    return purity;
  }

  public void setPurity(BigDecimal purity) {
    this.purity = purity;
  }

  public BigDecimal getPloidy() {
    return ploidy;
  }

  public void setPloidy(BigDecimal ploidy) {
    this.ploidy = ploidy;
  }

  public BigDecimal getDipLogR() {
    return dipLogR;
  }

  public void setDipLogR(BigDecimal dipLogR) {
    this.dipLogR = dipLogR;
  }
}
