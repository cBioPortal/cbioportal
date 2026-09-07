package org.cbioportal.application.rest.mapper;

import java.util.List;
import org.cbioportal.application.rest.response.PatientDTO;
import org.cbioportal.legacy.model.Patient;
import org.cbioportal.legacy.utils.Encoder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(uses = CancerStudyMapper.class, imports = Encoder.class)
public interface PatientMapper {
  PatientMapper INSTANCE = Mappers.getMapper(PatientMapper.class);

  @Mapping(target = "patientId", source = "stableId")
  @Mapping(target = "studyId", source = "cancerStudyIdentifier")
  @Mapping(target = "cancerStudy", source = "cancerStudy")
  @Mapping(target = "study", source = "cancerStudy")
  @Mapping(
      target = "uniquePatientKey",
      expression =
          "java(Encoder.calculateBase64(patient.getStableId(), patient.getCancerStudyIdentifier()))")
  @Mapping(target = "uniqueSampleKey", ignore = true)
  PatientDTO toDto(Patient patient);

  List<PatientDTO> toDtos(List<Patient> patients);
}
