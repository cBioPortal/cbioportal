package org.cbioportal.application.rest.mapper;

import java.util.List;
import org.cbioportal.application.rest.response.ClinicalDataEnrichmentDTO;
import org.cbioportal.legacy.model.ClinicalDataEnrichment;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(uses = LegacyClinicalAttributeMapper.class)
public interface LegacyClinicalDataEnrichmentMapper {
  LegacyClinicalDataEnrichmentMapper INSTANCE =
      Mappers.getMapper(LegacyClinicalDataEnrichmentMapper.class);

  ClinicalDataEnrichmentDTO toDto(ClinicalDataEnrichment enrichment);

  List<ClinicalDataEnrichmentDTO> toDtos(List<ClinicalDataEnrichment> enrichments);
}
