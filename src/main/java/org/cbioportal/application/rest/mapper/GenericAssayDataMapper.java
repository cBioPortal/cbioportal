package org.cbioportal.application.rest.mapper;

import java.util.List;
import org.cbioportal.application.rest.response.GenericAssayDataDTO;
import org.cbioportal.legacy.model.GenericAssayData;
import org.cbioportal.legacy.utils.Encoder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(imports = Encoder.class)
public interface GenericAssayDataMapper {
  GenericAssayDataMapper INSTANCE = Mappers.getMapper(GenericAssayDataMapper.class);

  @Mapping(
      target = "uniqueSampleKey",
      expression = "java( Encoder.calculateBase64(data.getSampleId(), data.getStudyId()) )")
  @Mapping(
      target = "uniquePatientKey",
      expression = "java( Encoder.calculateBase64(data.getPatientId(), data.getStudyId()) )")
  GenericAssayDataDTO toDto(GenericAssayData data);

  List<GenericAssayDataDTO> toDtos(List<GenericAssayData> dataList);
}
