package org.cbioportal.domain.embedding;

// A single point's (x, y) coordinates. customAttribute is a raw JSON string as stored in the
// database — EmbeddingDataMapper parses it into a JsonNode before it reaches the API response.
public record EmbeddingData(
    String patientId, String sampleId, Double x, Double y, String customAttribute) {}
