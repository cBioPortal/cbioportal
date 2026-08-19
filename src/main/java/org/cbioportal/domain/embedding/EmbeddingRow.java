package org.cbioportal.domain.embedding;

// One denormalized row per embedded point, as returned directly by ClickhouseEmbeddingMapper —
// embedding-definition and study fields are repeated on every row. EmbeddingUtil reshapes a list
// of these into the deduplicated Embedding aggregate. Field order must match the MyBatis
// resultMap's constructor-arg order in ClickhouseEmbeddingMapper.xml (bound positionally).
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
