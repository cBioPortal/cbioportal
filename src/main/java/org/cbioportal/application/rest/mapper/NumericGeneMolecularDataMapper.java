package org.cbioportal.application.rest.mapper;

import java.util.List;
import org.cbioportal.application.rest.response.NumericGeneMolecularDataDTO;
import org.cbioportal.legacy.model.GeneMolecularData;
import org.cbioportal.legacy.utils.Encoder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(imports = Encoder.class, uses = GeneMapper.class)
public interface NumericGeneMolecularDataMapper {
  NumericGeneMolecularDataMapper INSTANCE = Mappers.getMapper(NumericGeneMolecularDataMapper.class);

  @Mapping(
      target = "uniqueSampleKey",
      expression = "java( Encoder.calculateBase64(data.getSampleId(), data.getStudyId()) )")
  @Mapping(
      target = "uniquePatientKey",
      expression = "java( Encoder.calculateBase64(data.getPatientId(), data.getStudyId()) )")
  @Mapping(target = "value", expression = "java( new java.math.BigDecimal(data.getValue()) )")
  NumericGeneMolecularDataDTO toDto(GeneMolecularData data);

  List<NumericGeneMolecularDataDTO> toDtos(List<GeneMolecularData> dataList);
}
