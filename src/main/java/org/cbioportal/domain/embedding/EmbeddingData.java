package org.cbioportal.domain.embedding;

public record EmbeddingData(
    String patientId, String sampleId, Double x, Double y, String customAttribute) {}
