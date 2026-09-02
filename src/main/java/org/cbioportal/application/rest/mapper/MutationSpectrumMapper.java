package org.cbioportal.application.rest.mapper;

import java.util.List;
import org.cbioportal.application.rest.response.MutationSpectrumDTO;
import org.cbioportal.legacy.model.MutationSpectrum;
import org.cbioportal.legacy.utils.Encoder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(imports = Encoder.class)
public interface MutationSpectrumMapper {
  MutationSpectrumMapper INSTANCE = Mappers.getMapper(MutationSpectrumMapper.class);

  @Mapping(target = "CtoA", source = "ctoA")
  @Mapping(target = "CtoG", source = "ctoG")
  @Mapping(target = "CtoT", source = "ctoT")
  @Mapping(target = "TtoA", source = "ttoA")
  @Mapping(target = "TtoC", source = "ttoC")
  @Mapping(target = "TtoG", source = "ttoG")
  @Mapping(
      target = "uniqueSampleKey",
      expression =
          "java(Encoder.calculateBase64(mutationSpectrum.getSampleId(), mutationSpectrum.getStudyId()))")
  @Mapping(
      target = "uniquePatientKey",
      expression =
          "java(Encoder.calculateBase64(mutationSpectrum.getPatientId(), mutationSpectrum.getStudyId()))")
  MutationSpectrumDTO toDto(MutationSpectrum mutationSpectrum);

  List<MutationSpectrumDTO> toDtos(List<MutationSpectrum> mutationSpectrums);
}
