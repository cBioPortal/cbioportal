package org.cbioportal.application.rest.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.cbioportal.application.rest.response.EmbeddingDataDTO;
import org.cbioportal.domain.embedding.EmbeddingData;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface EmbeddingDataMapper {
  EmbeddingDataMapper INSTANCE = Mappers.getMapper(EmbeddingDataMapper.class);

  ObjectMapper JSON_MAPPER = new ObjectMapper();

  @Mapping(
      target = "data",
      expression = "java(parseCustomAttribute(embeddingData.customAttribute()))")
  EmbeddingDataDTO toEmbeddingDataDTO(EmbeddingData embeddingData);

  List<EmbeddingDataDTO> toEmbeddingDataDTOs(List<EmbeddingData> embeddingData);

  default JsonNode parseCustomAttribute(String customAttribute) {
    if (customAttribute == null || customAttribute.isBlank()) {
      return null;
    }
    try {
      return JSON_MAPPER.readTree(customAttribute);
    } catch (JsonProcessingException e) {
      return null;
    }
  }
}
