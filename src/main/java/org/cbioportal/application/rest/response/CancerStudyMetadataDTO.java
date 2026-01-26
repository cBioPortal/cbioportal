package org.cbioportal.application.rest.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import org.cbioportal.domain.cancerstudy.ResourceCount;
import org.cbioportal.domain.cancerstudy.TypeOfCancer;

@Schema(name = "CancerStudyMetadata", description = "Represents a cancer study")
public record CancerStudyMetadataDTO(
    String studyId,
    String cancerTypeId,
    String name,
    String description,
    Boolean publicStudy,
    String pmid,
    String citation,
    String groups,
    Integer status,
    String importDate,
    Integer allSampleCount,
    Integer sequencedSampleCount,
    Integer cnaSampleCount,
    Integer mrnaRnaSeqSampleCount,
    Integer mrnaRnaSeqV2SampleCount,
    Integer mrnaMicroarraySampleCount,
    Integer miRnaSampleCount,
    Integer methylationHm27SampleCount,
    Integer rppaSampleCount,
    Integer massSpectrometrySampleCount,
    Integer completeSampleCount,
    String referenceGenome,
    Integer treatmentCount,
    Integer structuralVariantCount,
    TypeOfCancer cancerType,
    Boolean readPermission,
    List<ResourceCount> resourceCounts) {}
