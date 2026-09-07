package org.cbioportal.application.rest.mapper;

import java.util.List;
import org.cbioportal.application.rest.response.GenomicDataBinDTO;
import org.cbioportal.legacy.model.GenomicDataBin;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface GenomicDataBinMapper {
  GenomicDataBinMapper INSTANCE = Mappers.getMapper(GenomicDataBinMapper.class);

  GenomicDataBinDTO toDTO(GenomicDataBin genomicDataBin);

  List<GenomicDataBinDTO> toDTOs(List<GenomicDataBin> genomicDataBins);
}
