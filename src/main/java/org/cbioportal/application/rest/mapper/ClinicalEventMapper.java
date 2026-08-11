package org.cbioportal.application.rest.mapper;

import java.util.List;
import org.cbioportal.application.rest.response.ClinicalEventDTO;
import org.cbioportal.legacy.model.ClinicalEvent;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ClinicalEventMapper {
  ClinicalEventMapper INSTANCE = Mappers.getMapper(ClinicalEventMapper.class);

  @Mapping(target = "startNumberOfDaysSinceDiagnosis", source = "startDate")
  @Mapping(target = "endNumberOfDaysSinceDiagnosis", source = "stopDate")
  ClinicalEventDTO toDto(ClinicalEvent event);

  List<ClinicalEventDTO> toDtos(List<ClinicalEvent> events);
}
