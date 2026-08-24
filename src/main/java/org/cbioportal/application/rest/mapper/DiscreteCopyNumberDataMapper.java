package org.cbioportal.application.rest.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import org.cbioportal.application.rest.response.DiscreteCopyNumberDataDTO;
import org.cbioportal.legacy.model.DiscreteCopyNumberData;
import org.cbioportal.legacy.utils.Encoder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(imports = Encoder.class, uses = GeneMapper.class)
public interface DiscreteCopyNumberDataMapper {
  DiscreteCopyNumberDataMapper INSTANCE = Mappers.getMapper(DiscreteCopyNumberDataMapper.class);
  ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  @Mapping(
      target = "uniqueSampleKey",
      expression = "java( Encoder.calculateBase64(data.getSampleId(), data.getStudyId()) )")
  @Mapping(
      target = "uniquePatientKey",
      expression = "java( Encoder.calculateBase64(data.getPatientId(), data.getStudyId()) )")
  @Mapping(
      target = "namespaceColumns",
      expression = "java(toNamespaceColumns(data.getAnnotationJson()))")
  DiscreteCopyNumberDataDTO toDto(DiscreteCopyNumberData data);

  List<DiscreteCopyNumberDataDTO> toDtos(List<DiscreteCopyNumberData> dataList);

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
          "Failed to parse discrete copy number annotation JSON for namespaceColumns", e);
    }
  }
}
