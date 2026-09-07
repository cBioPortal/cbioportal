package org.cbioportal.application.rest.mapper;

import java.util.List;
import org.cbioportal.application.rest.response.ClinicalDataBinDTO;
import org.cbioportal.legacy.model.ClinicalDataBin;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ClinicalDataBinMapper {
  ClinicalDataBinMapper INSTANCE = Mappers.getMapper(ClinicalDataBinMapper.class);

  ClinicalDataBinDTO toDTO(ClinicalDataBin clinicalDataBin);

  List<ClinicalDataBinDTO> toDTOs(List<ClinicalDataBin> clinicalDataBins);
}
