package org.cbioportal.domain.embedding;

import java.util.List;

// The domain-level result assembled by FetchEmbeddingInStudyUseCase from raw EmbeddingRows: one
// deduplicated EmbeddingDefinition plus the full set of per-point data across all matched studies.
public record Embedding(
    List<String> studyIdentifier,
    Integer totalSamples,
    Integer totalPatients,
    EmbeddingDefinition embeddingDefinition,
    List<EmbeddingData> embeddingData) {}
