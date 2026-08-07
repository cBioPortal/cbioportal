package org.cbioportal.domain.embedding;

import java.util.List;

public record Embedding(
    List<String> studyIdentifier,
    Integer totalSamples,
    Integer totalPatients,
    EmbeddingDefinition embeddingDefinition,
    List<EmbeddingData> embeddingData) {}
