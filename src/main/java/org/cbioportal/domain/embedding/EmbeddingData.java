package org.cbioportal.domain.embedding;

public record EmbeddingData(Integer embeddingDefinitionId, 
                            String patientId,
                            String sampleId,
                            double x,
                            double y,
                            String customAttribute,
                            String studyIdentifier) {
}
