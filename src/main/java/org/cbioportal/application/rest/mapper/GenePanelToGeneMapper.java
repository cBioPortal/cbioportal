package org.cbioportal.application.rest.mapper;

import java.util.List;
import org.cbioportal.application.rest.response.GenePanelToGeneDTO;
import org.cbioportal.legacy.model.GenePanelToGene;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface GenePanelToGeneMapper {
  GenePanelToGeneMapper INSTANCE = Mappers.getMapper(GenePanelToGeneMapper.class);

  GenePanelToGeneDTO toDto(GenePanelToGene genePanelToGene);

  List<GenePanelToGeneDTO> toDtos(List<GenePanelToGene> genePanelToGenes);
}
