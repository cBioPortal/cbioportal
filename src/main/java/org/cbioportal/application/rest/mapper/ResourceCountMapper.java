package org.cbioportal.application.rest.mapper;

import java.util.List;
import org.cbioportal.application.rest.response.ResourceCountDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ResourceCountMapper {
  ResourceCountMapper INSTANCE = Mappers.getMapper(ResourceCountMapper.class);

  @Mapping(target = "studyId", source = "cancerStudyIdentifier")
  ResourceCountDTO toDto(org.cbioportal.legacy.model.ResourceCount resourceCount);

  @Mapping(target = "studyId", source = "cancerStudyIdentifier")
  ResourceCountDTO toDto(org.cbioportal.domain.cancerstudy.ResourceCount resourceCount);

  List<ResourceCountDTO> toDtosFromLegacy(
      List<org.cbioportal.legacy.model.ResourceCount> resourceCounts);

  List<ResourceCountDTO> toDtosFromDomain(
      List<org.cbioportal.domain.cancerstudy.ResourceCount> resourceCounts);
}
