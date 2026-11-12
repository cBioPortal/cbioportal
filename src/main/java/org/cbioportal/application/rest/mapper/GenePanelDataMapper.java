package org.cbioportal.application.rest.mapper;

import java.util.List;
import org.cbioportal.application.rest.response.GenePanelDataDTO;
import org.cbioportal.legacy.model.GenePanelData;
import org.cbioportal.legacy.utils.Encoder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(imports = Encoder.class)
public interface GenePanelDataMapper {
  GenePanelDataMapper INSTANCE = Mappers.getMapper(GenePanelDataMapper.class);

  @Mapping(
      target = "uniqueSampleKey",
      expression = "java( Encoder.calculateBase64(data.getSampleId(), data.getStudyId()) )")
  @Mapping(
      target = "uniquePatientKey",
      expression = "java( Encoder.calculateBase64(data.getPatientId(), data.getStudyId()) )")
  GenePanelDataDTO toDto(GenePanelData data);

  List<GenePanelDataDTO> toDtos(List<GenePanelData> dataList);
}
