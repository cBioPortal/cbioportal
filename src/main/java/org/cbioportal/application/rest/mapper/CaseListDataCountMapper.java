package org.cbioportal.application.rest.mapper;

import java.util.List;
import org.cbioportal.application.rest.response.CaseListDataCountDTO;
import org.cbioportal.legacy.model.CaseListDataCount;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface CaseListDataCountMapper {
  CaseListDataCountMapper INSTANCE = Mappers.getMapper(CaseListDataCountMapper.class);

  CaseListDataCountDTO toDTO(CaseListDataCount caseListDataCount);

  List<CaseListDataCountDTO> toDTOs(List<CaseListDataCount> caseListDataCounts);
}
