package org.cbioportal.application.rest.mapper;

import java.util.List;
import org.cbioportal.application.rest.response.AlterationCountByGeneDTO;
import org.cbioportal.legacy.model.AlterationCountByGene;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface AlterationCountByGeneMapper {
  AlterationCountByGeneMapper INSTANCE = Mappers.getMapper(AlterationCountByGeneMapper.class);

  AlterationCountByGeneDTO toDTO(AlterationCountByGene alterationCountByGene);

  List<AlterationCountByGeneDTO> toDTOs(List<AlterationCountByGene> alterationCountByGene);
}
