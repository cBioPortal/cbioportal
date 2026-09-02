package org.cbioportal.application.rest.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(name = "CancerStudy", description = "Represents a cancer study")
public class CancerStudyDTO {
  private String studyId;
  private String cancerTypeId;
  private String name;
  private String description;
  private Boolean publicStudy;
  private String pmid;
  private String citation;
  private String groups;
  private Integer status;
  private String importDate;
  private TypeOfCancerDTO cancerType;
  private Integer allSampleCount;
  private Integer sequencedSampleCount;
  private Integer cnaSampleCount;
  private Integer mrnaRnaSeqSampleCount;
  private Integer mrnaRnaSeqV2SampleCount;
  private Integer mrnaMicroarraySampleCount;
  private Integer miRnaSampleCount;
  private Integer methylationHm27SampleCount;
  private Integer rppaSampleCount;
  private Integer massSpectrometrySampleCount;
  private Integer completeSampleCount;
  private String referenceGenome;
  private Boolean readPermission;
  private Integer treatmentCount;
  private Integer structuralVariantCount;
  private List<ResourceCountDTO> resourceCounts;

  public String getStudyId() {
    return studyId;
  }

  public void setStudyId(String studyId) {
    this.studyId = studyId;
  }

  public String getCancerTypeId() {
    return cancerTypeId;
  }

  public void setCancerTypeId(String cancerTypeId) {
    this.cancerTypeId = cancerTypeId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Boolean getPublicStudy() {
    return publicStudy;
  }

  public void setPublicStudy(Boolean publicStudy) {
    this.publicStudy = publicStudy;
  }

  public String getPmid() {
    return pmid;
  }

  public void setPmid(String pmid) {
    this.pmid = pmid;
  }

  public String getCitation() {
    return citation;
  }

  public void setCitation(String citation) {
    this.citation = citation;
  }

  public String getGroups() {
    return groups;
  }

  public void setGroups(String groups) {
    this.groups = groups;
  }

  public Integer getStatus() {
    return status;
  }

  public void setStatus(Integer status) {
    this.status = status;
  }

  public String getImportDate() {
    return importDate;
  }

  public void setImportDate(String importDate) {
    this.importDate = importDate;
  }

  public TypeOfCancerDTO getCancerType() {
    return cancerType;
  }

  public void setCancerType(TypeOfCancerDTO cancerType) {
    this.cancerType = cancerType;
  }

  public Integer getAllSampleCount() {
    return allSampleCount;
  }

  public void setAllSampleCount(Integer allSampleCount) {
    this.allSampleCount = allSampleCount;
  }

  public Integer getSequencedSampleCount() {
    return sequencedSampleCount;
  }

  public void setSequencedSampleCount(Integer sequencedSampleCount) {
    this.sequencedSampleCount = sequencedSampleCount;
  }

  public Integer getCnaSampleCount() {
    return cnaSampleCount;
  }

  public void setCnaSampleCount(Integer cnaSampleCount) {
    this.cnaSampleCount = cnaSampleCount;
  }

  public Integer getMrnaRnaSeqSampleCount() {
    return mrnaRnaSeqSampleCount;
  }

  public void setMrnaRnaSeqSampleCount(Integer mrnaRnaSeqSampleCount) {
    this.mrnaRnaSeqSampleCount = mrnaRnaSeqSampleCount;
  }

  public Integer getMrnaRnaSeqV2SampleCount() {
    return mrnaRnaSeqV2SampleCount;
  }

  public void setMrnaRnaSeqV2SampleCount(Integer mrnaRnaSeqV2SampleCount) {
    this.mrnaRnaSeqV2SampleCount = mrnaRnaSeqV2SampleCount;
  }

  public Integer getMrnaMicroarraySampleCount() {
    return mrnaMicroarraySampleCount;
  }

  public void setMrnaMicroarraySampleCount(Integer mrnaMicroarraySampleCount) {
    this.mrnaMicroarraySampleCount = mrnaMicroarraySampleCount;
  }

  public Integer getMiRnaSampleCount() {
    return miRnaSampleCount;
  }

  public void setMiRnaSampleCount(Integer miRnaSampleCount) {
    this.miRnaSampleCount = miRnaSampleCount;
  }

  public Integer getMethylationHm27SampleCount() {
    return methylationHm27SampleCount;
  }

  public void setMethylationHm27SampleCount(Integer methylationHm27SampleCount) {
    this.methylationHm27SampleCount = methylationHm27SampleCount;
  }

  public Integer getRppaSampleCount() {
    return rppaSampleCount;
  }

  public void setRppaSampleCount(Integer rppaSampleCount) {
    this.rppaSampleCount = rppaSampleCount;
  }

  public Integer getMassSpectrometrySampleCount() {
    return massSpectrometrySampleCount;
  }

  public void setMassSpectrometrySampleCount(Integer massSpectrometrySampleCount) {
    this.massSpectrometrySampleCount = massSpectrometrySampleCount;
  }

  public Integer getCompleteSampleCount() {
    return completeSampleCount;
  }

  public void setCompleteSampleCount(Integer completeSampleCount) {
    this.completeSampleCount = completeSampleCount;
  }

  public String getReferenceGenome() {
    return referenceGenome;
  }

  public void setReferenceGenome(String referenceGenome) {
    this.referenceGenome = referenceGenome;
  }

  public Boolean getReadPermission() {
    return readPermission;
  }

  public void setReadPermission(Boolean readPermission) {
    this.readPermission = readPermission;
  }

  public Integer getTreatmentCount() {
    return treatmentCount;
  }

  public void setTreatmentCount(Integer treatmentCount) {
    this.treatmentCount = treatmentCount;
  }

  public Integer getStructuralVariantCount() {
    return structuralVariantCount;
  }

  public void setStructuralVariantCount(Integer structuralVariantCount) {
    this.structuralVariantCount = structuralVariantCount;
  }

  public List<ResourceCountDTO> getResourceCounts() {
    return resourceCounts;
  }

  public void setResourceCounts(List<ResourceCountDTO> resourceCounts) {
    this.resourceCounts = resourceCounts;
  }
}
