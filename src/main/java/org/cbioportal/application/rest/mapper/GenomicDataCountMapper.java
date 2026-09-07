package org.cbioportal.application.rest.mapper;

import java.util.List;
import org.cbioportal.application.rest.response.GenomicDataCountDTO;
import org.cbioportal.legacy.model.GenomicDataCount;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface GenomicDataCountMapper {
  GenomicDataCountMapper INSTANCE = Mappers.getMapper(GenomicDataCountMapper.class);

  GenomicDataCountDTO toDTO(GenomicDataCount genomicDataCount);

  List<GenomicDataCountDTO> toDTOs(List<GenomicDataCount> genomicDataCounts);
}
