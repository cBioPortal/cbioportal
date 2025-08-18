package org.cbioportal.application.rest.mapper;

import java.util.List;
import org.cbioportal.application.rest.response.ClinicalAttributeDTO;
import org.cbioportal.domain.clinical_attributes.ClinicalAttribute;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ClinicalAttributeMapper {
  ClinicalAttributeMapper INSTANCE = Mappers.getMapper(ClinicalAttributeMapper.class);

  @Mapping(target = "clinicalAttributeId", source = "attrId")
  @Mapping(target = "studyId", source = "cancerStudyIdentifier")
  ClinicalAttributeDTO toClinicalAttributeDTO(ClinicalAttribute clinicalAttribute);

  List<ClinicalAttributeDTO> toClinicalAttributeDTOs(List<ClinicalAttribute> clinicalAttributes);
}
