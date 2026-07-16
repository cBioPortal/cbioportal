package org.cbioportal.application.rest.response;

public record EmbeddingDataDTO(
    String patientId, String sampleId, Double x, Double y, String customAttribute) {}
