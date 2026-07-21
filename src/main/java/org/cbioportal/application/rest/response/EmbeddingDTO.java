package org.cbioportal.application.rest.response;

import java.util.List;

public record EmbeddingDTO(
    List<String> studyIds,
    String title,
    String description,
    String embeddingType,
    Integer totalPatients,
    Integer sampleSize,
    List<EmbeddingDataDTO> data) {}
