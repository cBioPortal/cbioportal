package org.cbioportal.application.rest.mapper;

import java.util.List;
import org.cbioportal.application.rest.response.ClinicalEventTypeCountDTO;
import org.cbioportal.legacy.model.ClinicalEventTypeCount;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ClinicalEventTypeCountMapper {
  ClinicalEventTypeCountMapper INSTANCE = Mappers.getMapper(ClinicalEventTypeCountMapper.class);

  ClinicalEventTypeCountDTO toDTO(ClinicalEventTypeCount clinicalEventTypeCount);

  List<ClinicalEventTypeCountDTO> toDTOs(List<ClinicalEventTypeCount> clinicalEventTypeCounts);
}
