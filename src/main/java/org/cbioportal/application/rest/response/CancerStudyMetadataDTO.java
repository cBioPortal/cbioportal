package org.cbioportal.application.rest.response;

import io.swagger.v3.oas.annotations.media.Schema;
import org.cbioportal.domain.cancerstudy.TypeOfCancer;

@Schema(name = "CancerStudyMetadata", description = "Represents a cancer study")
public record CancerStudyMetadataDTO(
    String cancerStudyIdentifier,
    String typeOfCancerId,
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
    TypeOfCancer typeOfCancer,
    Boolean readPermission) {}
