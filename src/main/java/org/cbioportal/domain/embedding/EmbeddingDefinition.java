package org.cbioportal.domain.embedding;

import java.io.Serializable;

public record EmbeddingDefinition(
    Integer internalId,
    String description,
    String entityType,
    String reductionTechnique,
    String name,
    String shortName,
    String embeddingIdentifier)
    implements Serializable {
  // Summary constructor for queries that only join a subset of fields
  // (e.g. the points-fetch query, which only selects internalId/shortName/description/entityType)
  public EmbeddingDefinition(
      Integer internalId, String description, String entityType, String shortName) {
    this(internalId, description, entityType, null, null, shortName, null);
  }
}
