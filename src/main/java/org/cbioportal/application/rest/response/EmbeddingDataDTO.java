package org.cbioportal.application.rest.response;

import com.fasterxml.jackson.databind.JsonNode;

public record EmbeddingDataDTO(
    String patientId, String sampleId, Double x, Double y, JsonNode data) {}
