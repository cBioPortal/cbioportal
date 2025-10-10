package org.cbioportal.application.rest.mapper;

import org.cbioportal.application.rest.response.AlleleSpecificCopyNumberDTO;
import org.cbioportal.legacy.model.AlleleSpecificCopyNumber;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface AlleleSpecificCopyNumberMapper {
  AlleleSpecificCopyNumberMapper INSTANCE = Mappers.getMapper(AlleleSpecificCopyNumberMapper.class);

  AlleleSpecificCopyNumberDTO toAlleleSpecificCopyNumberDTO(
      AlleleSpecificCopyNumber alleleSpecificCopyNumber);
}
