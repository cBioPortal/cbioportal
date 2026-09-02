package org.cbioportal.application.rest.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import org.cbioportal.application.rest.response.MutationDTO;
import org.cbioportal.legacy.model.Mutation;
import org.cbioportal.legacy.utils.Encoder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(
    imports = Encoder.class,
    uses = {GeneMapper.class, AlleleSpecificCopyNumberMapper.class})
public interface MutationMapper {
  MutationMapper INSTANCE = Mappers.getMapper(MutationMapper.class);
  ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  @Mapping(
      target = "uniqueSampleKey",
      expression =
          "java( Encoder.calculateBase64(mutation.getSampleId()," + "mutation.getStudyId()) )")
  @Mapping(
      target = "uniquePatientKey",
      expression =
          "java( Encoder.calculateBase64(mutation.getPatientId(), " + "mutation.getStudyId()) )")
  @Mapping(source = "tumorSeqAllele", target = "variantAllele")
  @Mapping(target = "namespaceColumns", expression = "java(toNamespaceColumns(mutation))")
  MutationDTO toMutationDTOO(Mutation mutation);

  List<MutationDTO> toDTOs(List<Mutation> mutationList);

  default Map<String, Object> toNamespaceColumns(Mutation mutation) {
    Object annotationJson = mutation.getAnnotationJSON();
    if (annotationJson == null) {
      return null;
    }
    if (annotationJson instanceof Map<?, ?> map) {
      @SuppressWarnings("unchecked")
      Map<String, Object> castMap = (Map<String, Object>) map;
      return castMap;
    }
    try {
      return OBJECT_MAPPER.readValue(
          annotationJson.toString(), new TypeReference<Map<String, Object>>() {});
    } catch (JsonProcessingException e) {
      throw new IllegalArgumentException(
          "Failed to parse mutation annotation JSON for namespaceColumns", e);
    }
  }
}
