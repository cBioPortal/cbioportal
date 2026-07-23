package org.cbioportal.application.rest.mapper;

import org.cbioportal.application.rest.response.EmbeddingDTO;
import org.cbioportal.domain.embedding.Embedding;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(uses = {EmbeddingDataMapper.class})
public interface EmbeddingMapper {
  EmbeddingMapper INSTANCE = Mappers.getMapper(EmbeddingMapper.class);

  @Mapping(target = "studyIds", source = "studyIdentifier")
  @Mapping(target = "sampleSize", source = "totalSamples")
  @Mapping(target = "title", source = "embeddingDefinition.shortName")
  @Mapping(target = "description", source = "embeddingDefinition.description")
  @Mapping(target = "embeddingType", source = "embeddingDefinition.entityType")
  @Mapping(target = "data", source = "embeddingData")
  EmbeddingDTO toEmbeddingDTOO(Embedding embedding);
}
