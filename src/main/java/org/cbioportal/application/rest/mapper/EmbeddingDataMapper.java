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

/**
 * MapStruct mapper for the per-point embedding data. {@code customAttribute} is stored as a raw
 * JSON string in the database (it's an open-ended bag of extra fields per point), so it can't be
 * mapped directly — {@link #parseCustomAttribute(String)} parses it into a {@link JsonNode} so
 * clients receive real JSON rather than an escaped string.
 */
@Mapper
public interface EmbeddingDataMapper {
  EmbeddingDataMapper INSTANCE = Mappers.getMapper(EmbeddingDataMapper.class);

  ObjectMapper JSON_MAPPER = new ObjectMapper();

  /**
   * @param embeddingData a single point's coordinates plus its raw JSON custom attribute string
   * @return the DTO with {@code customAttribute} parsed into a {@link JsonNode}
   */
  @Mapping(
      target = "data",
      expression = "java(parseCustomAttribute(embeddingData.customAttribute()))")
  EmbeddingDataDTO toEmbeddingDataDTO(EmbeddingData embeddingData);

  List<EmbeddingDataDTO> toEmbeddingDataDTOs(List<EmbeddingData> embeddingData);

  /**
   * Parses {@code customAttribute} into a {@link JsonNode}, returning {@code null} for blank or
   * malformed input rather than failing the whole mapping.
   */
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
