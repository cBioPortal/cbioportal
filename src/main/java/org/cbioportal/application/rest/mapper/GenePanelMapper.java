package org.cbioportal.application.rest.mapper;

import java.util.List;
import org.cbioportal.application.rest.response.GenePanelDTO;
import org.cbioportal.legacy.model.GenePanel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(uses = GenePanelToGeneMapper.class)
public interface GenePanelMapper {
  GenePanelMapper INSTANCE = Mappers.getMapper(GenePanelMapper.class);

  @Mapping(target = "genePanelId", source = "stableId")
  GenePanelDTO toDto(GenePanel genePanel);

  List<GenePanelDTO> toDtos(List<GenePanel> genePanels);
}
