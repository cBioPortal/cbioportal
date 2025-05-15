package org.cbioportal.legacy.model;

import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;

public class CancerStudy implements ReadPermission, Serializable {

  private Integer cancerStudyId;
  @NotNull private String cancerStudyIdentifier;
  private String typeOfCancerId;
  private String name;
  private String description;
  private Boolean publicStudy;
  private String pmid;
  private String citation;
  private String groups;
  private Integer status;
  private Date importDate;
  private TypeOfCancer typeOfCancer;
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
  private Boolean readPermission = true;
  private Integer treatmentCount;
  private Integer structuralVariantCount;
  private Integer pathologySlideSampleCount;
  private Integer heSampleCount;
  private Integer mxifSampleCount;
  private Integer mxif2SampleCount;
  private Integer mxif3SampleCount;
  private Integer mxif4SampleCount;
  private Integer mxif5SampleCount;
  private Integer mxif6SampleCount;
  private Integer mxif7SampleCount;
  private Integer mxif8SampleCount;
  private Integer mxif9SampleCount;
  private Integer minervaStorySampleCount;
  private Integer cycifSampleCount;
  private Integer mpifSampleCount;
  private Integer mpif1SampleCount;
  private Integer mpif2SampleCount;
  private Integer mpif3SampleCount;
  private Integer mpif4SampleCount;
  private Integer ctSampleCount;

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

  public String getTypeOfCancerId() {
    return typeOfCancerId;
  }

  public void setTypeOfCancerId(String typeOfCancerId) {
    this.typeOfCancerId = typeOfCancerId;
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

  public Date getImportDate() {
    return importDate;
  }

  public void setImportDate(Date importDate) {
    this.importDate = importDate;
  }

  public TypeOfCancer getTypeOfCancer() {
    return typeOfCancer;
  }

  public void setTypeOfCancer(TypeOfCancer typeOfCancer) {
    this.typeOfCancer = typeOfCancer;
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

  public Integer getCompleteSampleCount() {
    return completeSampleCount;
  }

  public void setCompleteSampleCount(Integer completeSampleCount) {
    this.completeSampleCount = completeSampleCount;
  }

  public Integer getMassSpectrometrySampleCount() {
    return massSpectrometrySampleCount;
  }

  public void setMassSpectrometrySampleCount(Integer massSpectrometrySampleCount) {
    this.massSpectrometrySampleCount = massSpectrometrySampleCount;
  }

  public String getReferenceGenome() {
    return referenceGenome;
  }

  public void setReferenceGenome(String referenceGenome) {
    this.referenceGenome = referenceGenome;
  }

  @Override
  public void setReadPermission(Boolean permission) {
    this.readPermission = permission;
  }

  @Override
  public Boolean getReadPermission() {
    return readPermission;
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

  public Integer getPathologySlideSampleCount() {
    return pathologySlideSampleCount;
  }

  public void setPathologySlideSampleCount(Integer pathologySlideSampleCount) {
    this.pathologySlideSampleCount = pathologySlideSampleCount;
  }

  public Integer getHeSampleCount() {
    return heSampleCount;
  }

  public void setHeSampleCount(Integer heSampleCount) {
    this.heSampleCount = heSampleCount;
  }

  public Integer getMxifSampleCount() {
    return mxifSampleCount;
  }

  public void setMxifSampleCount(Integer mxifSampleCount) {
    this.mxifSampleCount = mxifSampleCount;
  }

  public Integer getMxif2SampleCount() {
    return mxif2SampleCount;
  }

  public void setMxif2SampleCount(Integer mxif2SampleCount) {
    this.mxif2SampleCount = mxif2SampleCount;
  }

  public Integer getMxif3SampleCount() {
    return mxif3SampleCount;
  }

  public void setMxif3SampleCount(Integer mxif3SampleCount) {
    this.mxif3SampleCount = mxif3SampleCount;
  }

  public Integer getMxif4SampleCount() {
    return mxif4SampleCount;
  }

  public void setMxif4SampleCount(Integer mxif4SampleCount) {
    this.mxif4SampleCount = mxif4SampleCount;
  }

  public Integer getMxif5SampleCount() {
    return mxif5SampleCount;
  }

  public void setMxif5SampleCount(Integer mxif5SampleCount) {
    this.mxif5SampleCount = mxif5SampleCount;
  }

  public Integer getMxif6SampleCount() {
    return mxif6SampleCount;
  }

  public void setMxif6SampleCount(Integer mxif6SampleCount) {
    this.mxif6SampleCount = mxif6SampleCount;
  }

  public Integer getMxif7SampleCount() {
    return mxif7SampleCount;
  }

  public void setMxif7SampleCount(Integer mxif7SampleCount) {
    this.mxif7SampleCount = mxif7SampleCount;
  }

  public Integer getMxif8SampleCount() {
    return mxif8SampleCount;
  }

  public void setMxif8SampleCount(Integer mxif8SampleCount) {
    this.mxif8SampleCount = mxif8SampleCount;
  }

  public Integer getMxif9SampleCount() {
    return mxif9SampleCount;
  }

  public void setMxif9SampleCount(Integer mxif9SampleCount) {
    this.mxif9SampleCount = mxif9SampleCount;
  }

  public Integer getMinervaStorySampleCount() {
    return minervaStorySampleCount;
  }

  public void setMinervaStorySampleCount(Integer minervaStorySampleCount) {
    this.minervaStorySampleCount = minervaStorySampleCount;
  }

  public Integer getCycifSampleCount() {
    return cycifSampleCount;
  }

  public void setCycifSampleCount(Integer cycifSampleCount) {
    this.cycifSampleCount = cycifSampleCount;
  }

  public Integer getMpifSampleCount() {
    return mpifSampleCount;
  }

  public void setMpifSampleCount(Integer mpifSampleCount) {
    this.mpifSampleCount = mpifSampleCount;
  }

  public Integer getMpif1SampleCount() {
    return mpif1SampleCount;
  }

  public void setMpif1SampleCount(Integer mpif1SampleCount) {
    this.mpif1SampleCount = mpif1SampleCount;
  }

  public Integer getMpif2SampleCount() {
    return mpif2SampleCount;
  }

  public void setMpif2SampleCount(Integer mpif2SampleCount) {
    this.mpif2SampleCount = mpif2SampleCount;
  }

  public Integer getMpif3SampleCount() {
    return mpif3SampleCount;
  }

  public void setMpif3SampleCount(Integer mpif3SampleCount) {
    this.mpif3SampleCount = mpif3SampleCount;
  }

  public Integer getMpif4SampleCount() {
    return mpif4SampleCount;
  }

  public void setMpif4SampleCount(Integer mpif4SampleCount) {
    this.mpif4SampleCount = mpif4SampleCount;
  }

  public Integer getCtSampleCount() {
    return ctSampleCount;
  }

  public void setCtSampleCount(Integer ctSampleCount) {
    this.ctSampleCount = ctSampleCount;
  }
}
