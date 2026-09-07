package org.cbioportal.application.rest.mapper;

import java.util.List;
import org.cbioportal.application.rest.response.ClinicalDataCountItemDTO;
import org.cbioportal.legacy.model.ClinicalDataCountItem;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ClinicalDataCountItemMapper {
  ClinicalDataCountItemMapper INSTANCE = Mappers.getMapper(ClinicalDataCountItemMapper.class);

  ClinicalDataCountItemDTO toDTO(ClinicalDataCountItem clinicalDataCountItem);

  List<ClinicalDataCountItemDTO> toDTOs(List<ClinicalDataCountItem> clinicalDataCountItems);
}
