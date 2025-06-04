package org.cbioportal.application.rest.mapper;

import java.util.List;
import org.cbioportal.application.rest.response.CancerStudyMetadataDTO;
import org.cbioportal.domain.cancerstudy.CancerStudyMetadata;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface CancerStudyMetadataMapper {
  CancerStudyMetadataMapper INSTANCE = Mappers.getMapper(CancerStudyMetadataMapper.class);

  @Mapping(target = "importDate", source = "importDate", dateFormat = "yyyy-MM-dd HH:mm:ss")
  @Mapping(target = "studyId", source = "cancerStudyIdentifier")
  @Mapping(target = "cancerTypeId", source = "typeOfCancerId")
  // TODO: ReadPermission needs to be implemented
  @Mapping(target = "readPermission", source = "publicStudy")
  CancerStudyMetadataDTO toDto(CancerStudyMetadata cancerStudyMetadata);

  @Mapping(target = "importDate", source = "importDate", dateFormat = "yyyy-MM-dd HH:mm:ss")
  @Mapping(target = "studyId", source = "cancerStudyIdentifier")
  @Mapping(target = "cancerTypeId", source = "typeOfCancerId")
  // TODO: ReadPermission needs to be implemented
  @Mapping(target = "readPermission", source = "publicStudy")
  List<CancerStudyMetadataDTO> toDtos(List<CancerStudyMetadata> cancerStudyMetadataList);
}
