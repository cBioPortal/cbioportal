package org.cbioportal.application.rest.mapper;

import java.util.List;
import org.cbioportal.application.rest.response.ClinicalDataDTO;
import org.cbioportal.domain.clinical_data.ClinicalData;
import org.cbioportal.legacy.utils.Encoder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(imports = Encoder.class, uses = ClinicalAttributeMapper.class)
public interface ClinicalDataMapper {
  ClinicalDataMapper INSTANCE = Mappers.getMapper(ClinicalDataMapper.class);

  @Mapping(
      target = "uniqueSampleKey",
      expression =
          "java( Encoder.calculateBase64(clinicalData.sampleId()," + "clinicalData.studyId()) )")
  @Mapping(
      target = "uniquePatientKey",
      expression =
          "java( Encoder.calculateBase64(clinicalData.patientId(), " + "clinicalData.studyId()) )")
  @Mapping(target = "patientAttribute", source = "clinicalAttribute.patientAttribute")
  @Mapping(target = "clinicalAttributeId", source = "attrId")
  @Mapping(target = "value", source = "attrValue")
  ClinicalDataDTO toClinicalDataDTO(ClinicalData clinicalData);

  List<ClinicalDataDTO> toDTOs(List<ClinicalData> clinicalDataList);
}
