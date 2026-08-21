package org.cbioportal.application.rest.mapper;

import java.util.List;
import org.cbioportal.application.rest.response.GenericAssayMetaDTO;
import org.cbioportal.legacy.model.meta.GenericAssayMeta;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface GenericAssayMetaMapper {
  GenericAssayMetaMapper INSTANCE = Mappers.getMapper(GenericAssayMetaMapper.class);

  GenericAssayMetaDTO toDTO(GenericAssayMeta genericAssayMeta);

  List<GenericAssayMetaDTO> toDTOs(List<GenericAssayMeta> genericAssayMeta);
}
