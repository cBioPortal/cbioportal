package org.cbioportal.application.rest.mapper;

import java.util.List;
import org.cbioportal.application.rest.response.GenericAssayDataBinDTO;
import org.cbioportal.legacy.model.GenericAssayDataBin;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface GenericAssayDataBinMapper {
  GenericAssayDataBinMapper INSTANCE = Mappers.getMapper(GenericAssayDataBinMapper.class);

  GenericAssayDataBinDTO toDTO(GenericAssayDataBin genericAssayDataBin);

  List<GenericAssayDataBinDTO> toDTOs(List<GenericAssayDataBin> genericAssayDataBins);
}
