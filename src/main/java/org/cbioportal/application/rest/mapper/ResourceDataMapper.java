package org.cbioportal.application.rest.mapper;

import java.util.List;
import org.cbioportal.application.rest.response.ResourceDataDTO;
import org.cbioportal.legacy.model.ResourceData;
import org.cbioportal.legacy.utils.Encoder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(uses = ResourceDefinitionMapper.class, imports = Encoder.class)
public interface ResourceDataMapper {
  ResourceDataMapper INSTANCE = Mappers.getMapper(ResourceDataMapper.class);

  @Mapping(
      target = "uniqueSampleKey",
      expression =
          "java( resourceData.getSampleId() == null ? null : "
              + "Encoder.calculateBase64(resourceData.getSampleId(), resourceData.getStudyId()) )")
  @Mapping(
      target = "uniquePatientKey",
      expression =
          "java( resourceData.getPatientId() == null ? null : "
              + "Encoder.calculateBase64(resourceData.getPatientId(), resourceData.getStudyId()) )")
  ResourceDataDTO toDto(ResourceData resourceData);

  List<ResourceDataDTO> toDtos(List<ResourceData> resourceDataList);
}
