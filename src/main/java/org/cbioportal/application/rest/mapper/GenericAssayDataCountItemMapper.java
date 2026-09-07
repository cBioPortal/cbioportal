package org.cbioportal.application.rest.mapper;

import java.util.List;
import org.cbioportal.application.rest.response.GenericAssayDataCountItemDTO;
import org.cbioportal.legacy.model.GenericAssayDataCountItem;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface GenericAssayDataCountItemMapper {
  GenericAssayDataCountItemMapper INSTANCE =
      Mappers.getMapper(GenericAssayDataCountItemMapper.class);

  GenericAssayDataCountItemDTO toDTO(GenericAssayDataCountItem genericAssayDataCountItem);

  List<GenericAssayDataCountItemDTO> toDTOs(
      List<GenericAssayDataCountItem> genericAssayDataCountItems);
}
