package org.cbioportal.application.rest.mapper;

import java.util.List;
import org.cbioportal.application.rest.response.SampleDTO;
import org.cbioportal.domain.sample.Sample;
import org.cbioportal.legacy.utils.Encoder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(imports = Encoder.class)
public interface SampleMapper {
  SampleMapper INSTANCE = Mappers.getMapper(SampleMapper.class);

  @Mapping(target = "patientId", source = "patientStableId")
  @Mapping(target = "sampleId", source = "stableId")
  @Mapping(target = "studyId", source = "cancerStudyIdentifier")
  @Mapping(
      target = "uniqueSampleKey",
      expression =
          "java( Encoder.calculateBase64(sample.stableId()," + "sample.cancerStudyIdentifier()) )")
  @Mapping(
      target = "uniquePatientKey",
      expression =
          "java( Encoder.calculateBase64(sample.patientStableId(), "
              + "sample.cancerStudyIdentifier()) )")
  SampleDTO toSampleDTO(Sample sample);

  List<SampleDTO> toDtos(List<Sample> samples);
}
