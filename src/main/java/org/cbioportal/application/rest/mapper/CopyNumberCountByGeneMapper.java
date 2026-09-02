package org.cbioportal.application.rest.mapper;

import java.util.List;
import org.cbioportal.application.rest.response.CopyNumberCountByGeneDTO;
import org.cbioportal.legacy.model.CopyNumberCountByGene;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface CopyNumberCountByGeneMapper {
  CopyNumberCountByGeneMapper INSTANCE = Mappers.getMapper(CopyNumberCountByGeneMapper.class);

  CopyNumberCountByGeneDTO toDTO(CopyNumberCountByGene copyNumberCountByGene);

  List<CopyNumberCountByGeneDTO> toDTOs(List<CopyNumberCountByGene> copyNumberCountByGene);
}
