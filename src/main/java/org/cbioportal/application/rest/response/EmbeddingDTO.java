package org.cbioportal.application.rest.response;

import java.util.List;
import org.cbioportal.domain.embedding.EmbeddingData;
import org.cbioportal.domain.embedding.EmbeddingDefinition;

public record EmbeddingDTO(
    List<String> studyIdentifier,
    Integer totalSample,
    Integer totalPatient,
    EmbeddingDefinition embeddingDefinition,
    List<EmbeddingData> embeddingData) {}
