package org.cbioportal.application.rest.mapper;

import org.cbioportal.application.rest.response.EmbeddingDTO;
import org.cbioportal.domain.embedding.Embedding;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * MapStruct mapper that converts the domain-level {@link Embedding} (assembled from raw
 * repository rows) into the {@link EmbeddingDTO} returned by the REST API. Several target fields
 * are pulled up from the nested {@code embeddingDefinition}, since the DTO is intentionally flat.
 */
@Mapper(uses = {EmbeddingDataMapper.class})
public interface EmbeddingMapper {
  EmbeddingMapper INSTANCE = Mappers.getMapper(EmbeddingMapper.class);

  /**
   * @param embedding the domain object combining study/sample counts, the embedding definition,
   *     and per-point data
   * @return the flattened {@link EmbeddingDTO} ready for JSON serialization
   */
  @Mapping(target = "studyIds", source = "studyIdentifier")
  @Mapping(target = "sampleSize", source = "totalSamples")
  @Mapping(target = "title", source = "embeddingDefinition.shortName")
  @Mapping(target = "description", source = "embeddingDefinition.description")
  @Mapping(target = "embeddingType", source = "embeddingDefinition.entityType")
  @Mapping(target = "data", source = "embeddingData")
  EmbeddingDTO toEmbeddingDTO(Embedding embedding);
}
