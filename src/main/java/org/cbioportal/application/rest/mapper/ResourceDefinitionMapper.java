package org.cbioportal.application.rest.mapper;

import java.util.List;
import org.cbioportal.application.rest.response.ResourceDefinitionDTO;
import org.cbioportal.legacy.model.ResourceDefinition;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ResourceDefinitionMapper {
  ResourceDefinitionMapper INSTANCE = Mappers.getMapper(ResourceDefinitionMapper.class);

  @Mapping(target = "studyId", source = "cancerStudyIdentifier")
  ResourceDefinitionDTO toDto(ResourceDefinition resourceDefinition);

  List<ResourceDefinitionDTO> toDtos(List<ResourceDefinition> resourceDefinitions);
}
