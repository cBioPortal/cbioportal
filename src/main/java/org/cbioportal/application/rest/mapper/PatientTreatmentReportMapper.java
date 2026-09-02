package org.cbioportal.application.rest.mapper;

import org.cbioportal.application.rest.response.PatientTreatmentReportDTO;
import org.cbioportal.legacy.model.PatientTreatmentReport;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface PatientTreatmentReportMapper {
  PatientTreatmentReportMapper INSTANCE = Mappers.getMapper(PatientTreatmentReportMapper.class);

  PatientTreatmentReportDTO toDTO(PatientTreatmentReport patientTreatmentReport);
}
