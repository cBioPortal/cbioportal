package org.cbioportal.application.rest.mapper;

import java.util.List;
import org.cbioportal.application.rest.response.ClinicalAttributeDTO;
import org.cbioportal.legacy.model.ClinicalAttribute;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface LegacyClinicalAttributeMapper {
  LegacyClinicalAttributeMapper INSTANCE = Mappers.getMapper(LegacyClinicalAttributeMapper.class);

  @Mapping(target = "clinicalAttributeId", source = "attrId")
  @Mapping(target = "studyId", source = "cancerStudyIdentifier")
  ClinicalAttributeDTO toDto(ClinicalAttribute clinicalAttribute);

  List<ClinicalAttributeDTO> toDtos(List<ClinicalAttribute> clinicalAttributes);
}
