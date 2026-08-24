package org.cbioportal.application.rest.mapper;

import java.util.List;
import org.cbioportal.application.rest.response.GenomicDataCountItemDTO;
import org.cbioportal.legacy.model.GenomicDataCountItem;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(uses = GenomicDataCountMapper.class)
public interface GenomicDataCountItemMapper {
  GenomicDataCountItemMapper INSTANCE = Mappers.getMapper(GenomicDataCountItemMapper.class);

  GenomicDataCountItemDTO toDTO(GenomicDataCountItem genomicDataCountItem);

  List<GenomicDataCountItemDTO> toDTOs(List<GenomicDataCountItem> genomicDataCountItems);
}
