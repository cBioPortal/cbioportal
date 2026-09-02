package org.cbioportal.application.rest.mapper;

import java.util.List;
import org.cbioportal.application.rest.response.GenesetDTO;
import org.cbioportal.legacy.model.Geneset;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface GenesetMapper {
  GenesetMapper INSTANCE = Mappers.getMapper(GenesetMapper.class);

  GenesetDTO toDto(Geneset geneset);

  List<GenesetDTO> toDtos(List<Geneset> genesets);
}
