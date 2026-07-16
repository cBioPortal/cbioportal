package org.cbioportal.application.rest.response;

public record EmbeddingDefinitionDTO(
    String description,
    String entityType,
    String reductionTechnique,
    String name,
    String shortName,
    String embeddingIdentifier) {}
