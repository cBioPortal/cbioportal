package org.cbioportal.application.rest.mapper;

import java.util.List;
import org.cbioportal.application.rest.response.GenesetMolecularDataDTO;
import org.cbioportal.legacy.model.GenesetMolecularData;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface GenesetMolecularDataMapper {
  GenesetMolecularDataMapper INSTANCE = Mappers.getMapper(GenesetMolecularDataMapper.class);

  @Mapping(target = "geneticProfileId", source = "molecularProfileId")
  GenesetMolecularDataDTO toDto(GenesetMolecularData data);

  List<GenesetMolecularDataDTO> toDtos(List<GenesetMolecularData> data);
}
