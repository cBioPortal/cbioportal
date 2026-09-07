package org.cbioportal.application.rest.mapper;

import java.util.List;
import org.cbioportal.application.rest.response.GenesetCorrelationDTO;
import org.cbioportal.legacy.model.GenesetCorrelation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface GenesetCorrelationMapper {
  GenesetCorrelationMapper INSTANCE = Mappers.getMapper(GenesetCorrelationMapper.class);

  @Mapping(target = "expressionGeneticProfileId", source = "expressionMolecularProfileId")
  @Mapping(target = "zScoreGeneticProfileId", source = "zScoreMolecularProfileId")
  GenesetCorrelationDTO toDto(GenesetCorrelation data);

  List<GenesetCorrelationDTO> toDtos(List<GenesetCorrelation> data);
}
