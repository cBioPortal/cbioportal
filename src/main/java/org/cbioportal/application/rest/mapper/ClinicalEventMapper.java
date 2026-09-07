package org.cbioportal.application.rest.mapper;

import java.util.List;
import org.cbioportal.application.rest.response.ClinicalEventDTO;
import org.cbioportal.legacy.model.ClinicalEvent;
import org.cbioportal.legacy.utils.Encoder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(imports = Encoder.class)
public interface ClinicalEventMapper {
  ClinicalEventMapper INSTANCE = Mappers.getMapper(ClinicalEventMapper.class);

  @Mapping(target = "startNumberOfDaysSinceDiagnosis", source = "startDate")
  @Mapping(target = "endNumberOfDaysSinceDiagnosis", source = "stopDate")
  @Mapping(
      target = "uniquePatientKey",
      expression =
          "java(event.getUniquePatientKey() != null ? event.getUniquePatientKey() : "
              + "(event.getPatientId() == null || event.getStudyId() == null ? null "
              + ": Encoder.calculateBase64(event.getPatientId(), event.getStudyId())))")
  ClinicalEventDTO toDto(ClinicalEvent event);

  List<ClinicalEventDTO> toDtos(List<ClinicalEvent> events);
}
