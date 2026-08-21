package org.cbioportal.application.rest.mapper;

import java.util.List;
import org.cbioportal.application.rest.response.CopyNumberSegDTO;
import org.cbioportal.legacy.model.CopyNumberSeg;
import org.cbioportal.legacy.utils.Encoder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(imports = Encoder.class)
public interface CopyNumberSegMapper {
  CopyNumberSegMapper INSTANCE = Mappers.getMapper(CopyNumberSegMapper.class);

  @Mapping(target = "studyId", source = "cancerStudyIdentifier")
  @Mapping(target = "sampleId", source = "sampleStableId")
  @Mapping(target = "chromosome", source = "chr")
  @Mapping(target = "numberOfProbes", source = "numProbes")
  @Mapping(
      target = "uniqueSampleKey",
      expression =
          "java(Encoder.calculateBase64(copyNumberSeg.getSampleStableId(), copyNumberSeg.getCancerStudyIdentifier()))")
  @Mapping(
      target = "uniquePatientKey",
      expression =
          "java(Encoder.calculateBase64(copyNumberSeg.getPatientId(), copyNumberSeg.getCancerStudyIdentifier()))")
  CopyNumberSegDTO toDto(CopyNumberSeg copyNumberSeg);

  List<CopyNumberSegDTO> toDtos(List<CopyNumberSeg> copyNumberSegs);
}
