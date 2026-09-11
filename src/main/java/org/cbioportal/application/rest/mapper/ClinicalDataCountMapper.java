package org.cbioportal.application.rest.mapper;

import java.util.List;
import org.cbioportal.application.rest.response.ClinicalDataCountDTO;
import org.cbioportal.legacy.model.ClinicalDataCount;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ClinicalDataCountMapper {
  ClinicalDataCountMapper INSTANCE = Mappers.getMapper(ClinicalDataCountMapper.class);

  ClinicalDataCountDTO toDto(ClinicalDataCount clinicalDataCount);

  List<ClinicalDataCountDTO> toDtos(List<ClinicalDataCount> clinicalDataCounts);
}
