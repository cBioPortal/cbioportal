package org.cbioportal.application.rest.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record EmbeddingDTO(
    List<String> studyIds,
    String title,
    String description,
    @JsonProperty("embedding_type") String embeddingType,
    Integer totalPatients,
    Integer sampleSize,
    List<EmbeddingDataDTO> data) {}
