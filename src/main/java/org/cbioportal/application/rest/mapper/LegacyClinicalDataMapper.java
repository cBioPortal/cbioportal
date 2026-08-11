package org.cbioportal.application.rest.mapper;

import java.util.List;
import org.cbioportal.application.rest.response.ClinicalDataDTO;
import org.cbioportal.legacy.model.ClinicalData;
import org.cbioportal.legacy.utils.Encoder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(imports = Encoder.class, uses = LegacyClinicalAttributeMapper.class)
public interface LegacyClinicalDataMapper {
  LegacyClinicalDataMapper INSTANCE = Mappers.getMapper(LegacyClinicalDataMapper.class);

  @Mapping(
      target = "uniqueSampleKey",
      expression =
          "java(Encoder.calculateBase64(clinicalData.getSampleId(), clinicalData.getStudyId()))")
  @Mapping(
      target = "uniquePatientKey",
      expression =
          "java(Encoder.calculateBase64(clinicalData.getPatientId(), clinicalData.getStudyId()))")
  @Mapping(target = "patientAttribute", source = "clinicalAttribute.patientAttribute")
  @Mapping(target = "clinicalAttributeId", source = "attrId")
  @Mapping(target = "value", source = "attrValue")
  ClinicalDataDTO toDto(ClinicalData clinicalData);

  List<ClinicalDataDTO> toDtos(List<ClinicalData> clinicalDataList);
}
