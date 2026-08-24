package org.cbioportal.application.rest.mapper;

import java.util.Collection;
import java.util.List;
import org.cbioportal.application.rest.response.AlterationEnrichmentDTO;
import org.cbioportal.legacy.model.AlterationEnrichment;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface AlterationEnrichmentMapper {
  AlterationEnrichmentMapper INSTANCE = Mappers.getMapper(AlterationEnrichmentMapper.class);

  AlterationEnrichmentDTO toDTO(AlterationEnrichment alterationEnrichment);

  List<AlterationEnrichmentDTO> toDTOs(List<AlterationEnrichment> alterationEnrichment);

  Collection<AlterationEnrichmentDTO> toDTOs(Collection<AlterationEnrichment> alterationEnrichment);
}
