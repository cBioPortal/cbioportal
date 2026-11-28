package org.cbioportal.application.rest.mapper;

import java.util.List;
import org.cbioportal.application.rest.response.CancerStudyDTO;
import org.cbioportal.domain.cancerstudy.CancerStudyMetadata;
import org.cbioportal.legacy.model.CancerStudy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(uses = {TypeOfCancerMapper.class, ResourceCountMapper.class, DateMapper.class})
public interface CancerStudyMapper {
  CancerStudyMapper INSTANCE = Mappers.getMapper(CancerStudyMapper.class);

  @Mapping(target = "studyId", source = "cancerStudyIdentifier")
  @Mapping(target = "cancerTypeId", source = "typeOfCancerId")
  @Mapping(target = "cancerType", source = "typeOfCancer")
  @Mapping(target = "importDate", source = "importDate", qualifiedByName = "utcDateTime")
  CancerStudyDTO toDto(CancerStudy cancerStudy);

  @Mapping(target = "studyId", source = "cancerStudyIdentifier")
  @Mapping(target = "cancerTypeId", source = "typeOfCancerId")
  @Mapping(target = "importDate", source = "importDate", qualifiedByName = "utcDateTime")
  @Mapping(target = "readPermission", source = "publicStudy")
  CancerStudyDTO toDto(CancerStudyMetadata cancerStudyMetadata);

  @Mapping(target = "studyId", source = "cancerStudyMetadata.cancerStudyIdentifier")
  @Mapping(target = "cancerTypeId", source = "cancerStudyMetadata.typeOfCancerId")
  @Mapping(
      target = "importDate",
      source = "cancerStudyMetadata.importDate",
      qualifiedByName = "utcDateTime")
  @Mapping(target = "readPermission", source = "readPermission")
  CancerStudyDTO toDto(CancerStudyMetadata cancerStudyMetadata, boolean readPermission);

  List<CancerStudyDTO> toDtosFromCancerStudies(List<CancerStudy> cancerStudies);

  List<CancerStudyDTO> toDtosFromMetadata(List<CancerStudyMetadata> cancerStudyMetadataList);
}
