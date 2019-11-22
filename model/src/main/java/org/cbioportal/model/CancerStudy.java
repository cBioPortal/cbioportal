package org.cbioportal.model;

import java.io.Serializable;
import java.util.Date;
import javax.validation.constraints.NotNull;

public class CancerStudy implements Serializable {

    private Integer cancerStudyId;
    @NotNull
    private String cancerStudyIdentifier;
    private String typeOfCancerId;
    private String name;
    private String shortName;
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
    private Integer completeSampleCount;
    private String referenceGenome;

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

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
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
    
    public String getReferenceGenome() { return  referenceGenome; }
    
    public void setReferenceGenome(String referenceGenome) { this.referenceGenome = referenceGenome; }
}