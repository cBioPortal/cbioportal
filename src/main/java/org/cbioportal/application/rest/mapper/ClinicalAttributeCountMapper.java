package org.cbioportal.application.rest.mapper;

import java.util.List;
import org.cbioportal.application.rest.response.ClinicalAttributeCountDTO;
import org.cbioportal.legacy.model.ClinicalAttributeCount;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ClinicalAttributeCountMapper {
  ClinicalAttributeCountMapper INSTANCE = Mappers.getMapper(ClinicalAttributeCountMapper.class);

  @Mapping(target = "clinicalAttributeId", source = "attrId")
  ClinicalAttributeCountDTO toDto(ClinicalAttributeCount count);

  List<ClinicalAttributeCountDTO> toDtos(List<ClinicalAttributeCount> counts);
}
