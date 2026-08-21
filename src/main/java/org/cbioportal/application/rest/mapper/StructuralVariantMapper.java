package org.cbioportal.application.rest.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import org.cbioportal.application.rest.response.StructuralVariantDTO;
import org.cbioportal.legacy.model.StructuralVariant;
import org.cbioportal.legacy.utils.Encoder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(imports = Encoder.class)
public interface StructuralVariantMapper {
  StructuralVariantMapper INSTANCE = Mappers.getMapper(StructuralVariantMapper.class);
  ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  @Mapping(
      target = "uniqueSampleKey",
      expression =
          "java( Encoder.calculateBase64(structuralVariant.getSampleId(), structuralVariant.getStudyId()) )")
  @Mapping(
      target = "uniquePatientKey",
      expression =
          "java( Encoder.calculateBase64(structuralVariant.getPatientId(), structuralVariant.getStudyId()) )")
  @Mapping(
      target = "namespaceColumns",
      expression = "java(toNamespaceColumns(structuralVariant.getAnnotationJson()))")
  StructuralVariantDTO toDto(StructuralVariant structuralVariant);

  List<StructuralVariantDTO> toDtos(List<StructuralVariant> structuralVariants);

  default Map<String, Map<String, Object>> toNamespaceColumns(Object annotationJson) {
    if (annotationJson == null) {
      return null;
    }
    if (annotationJson instanceof Map<?, ?> map) {
      @SuppressWarnings("unchecked")
      Map<String, Map<String, Object>> castMap = (Map<String, Map<String, Object>>) map;
      return castMap;
    }
    try {
      return OBJECT_MAPPER.readValue(
          annotationJson.toString(), new TypeReference<Map<String, Map<String, Object>>>() {});
    } catch (JsonProcessingException e) {
      throw new IllegalArgumentException(
          "Failed to parse structural variant annotation JSON for namespaceColumns", e);
    }
  }
}
