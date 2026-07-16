package org.cbioportal.domain.embedding;

import java.util.List;

public record Embedding(
    List<String> studyIdentifier,
    Integer totalSample,
    Integer totalPatient,
    EmbeddingDefinition embeddingDefinition,
    List<EmbeddingData> embeddingData) {}
