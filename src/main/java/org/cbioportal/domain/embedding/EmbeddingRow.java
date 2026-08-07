package org.cbioportal.domain.embedding;

public record EmbeddingRow(
    Integer internalId,
    String shortName,
    String description,
    String entityType,
    String reductionTechnique,
    String name,
    String embeddingIdentifier,
    String sampleId,
    String patientId,
    Double x,
    Double y,
    String customAttribute,
    String studyIdentifier,
    Integer embeddingId) {}
