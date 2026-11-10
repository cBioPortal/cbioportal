package org.cbioportal.application.rest.mapper;

import java.util.List;
import org.cbioportal.application.rest.response.GenesetMolecularDataDTO;
import org.cbioportal.legacy.model.GenesetMolecularData;
import org.cbioportal.legacy.utils.Encoder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(imports = Encoder.class)
public interface GenesetMolecularDataMapper {
  GenesetMolecularDataMapper INSTANCE = Mappers.getMapper(GenesetMolecularDataMapper.class);

  @Mapping(target = "geneticProfileId", source = "molecularProfileId")
  @Mapping(
      target = "uniqueSampleKey",
      expression = "java( Encoder.calculateBase64(data.getSampleId(), data.getStudyId()) )")
  @Mapping(
      target = "uniquePatientKey",
      expression = "java( Encoder.calculateBase64(data.getPatientId(), data.getStudyId()) )")
  GenesetMolecularDataDTO toDto(GenesetMolecularData data);

  List<GenesetMolecularDataDTO> toDtos(List<GenesetMolecularData> data);
}
