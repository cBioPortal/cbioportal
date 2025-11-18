package org.cbioportal.application.rest.mapper;

import java.util.List;
import org.cbioportal.application.rest.response.ClinicalDataEnrichmentDTO;
import org.cbioportal.domain.clinical_data_enrichment.ClinicalDataEnrichment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * MapStruct mapper for converting between ClinicalDataEnrichment domain objects and DTOs.
 *
 * <p>This mapper handles the conversion of clinical data enrichment results from the domain layer
 * to REST API response DTOs. It uses MapStruct for compile-time code generation and delegates
 * clinical attribute mapping to {@link ClinicalAttributeMapper}.
 *
 * <p>Key mapping transformations:
 *
 * <ul>
 *   <li>Converts EnrichmentTestMethod enum to its display name string
 *   <li>Delegates ClinicalAttribute to ClinicalAttributeDTO mapping
 *   <li>Passes through score and pValue directly
 * </ul>
 *
 * @see ClinicalDataEnrichment
 * @see ClinicalDataEnrichmentDTO
 * @see ClinicalAttributeMapper
 */
@Mapper(uses = {ClinicalAttributeMapper.class})
public interface ClinicalDataEnrichmentMapper {
  ClinicalDataEnrichmentMapper INSTANCE = Mappers.getMapper(ClinicalDataEnrichmentMapper.class);

  /**
   * Converts a single ClinicalDataEnrichment domain object to a DTO.
   *
   * @param clinicalDataEnrichment the domain object to convert
   * @return the corresponding DTO for API response
   */
  @Mapping(target = "method", expression = "java(clinicalDataEnrichment.method().getDisplayName())")
  ClinicalDataEnrichmentDTO toClinicalDataEnrichmentDTO(
      ClinicalDataEnrichment clinicalDataEnrichment);

  /**
   * Converts a list of ClinicalDataEnrichment domain objects to DTOs.
   *
   * @param clinicalDataEnrichments the list of domain objects to convert
   * @return the corresponding list of DTOs for API response
   */
  List<ClinicalDataEnrichmentDTO> toDTOs(List<ClinicalDataEnrichment> clinicalDataEnrichments);
}
