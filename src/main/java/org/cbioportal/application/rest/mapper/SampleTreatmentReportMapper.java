package org.cbioportal.application.rest.mapper;

import org.cbioportal.application.rest.response.SampleTreatmentReportDTO;
import org.cbioportal.legacy.model.SampleTreatmentReport;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface SampleTreatmentReportMapper {
  SampleTreatmentReportMapper INSTANCE = Mappers.getMapper(SampleTreatmentReportMapper.class);

  SampleTreatmentReportDTO toDTO(SampleTreatmentReport sampleTreatmentReport);
}
