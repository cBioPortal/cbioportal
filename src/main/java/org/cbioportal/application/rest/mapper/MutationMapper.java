package org.cbioportal.application.rest.mapper;

import java.util.List;
import org.cbioportal.application.rest.response.MutationDTO;
import org.cbioportal.legacy.model.Mutation;
import org.cbioportal.legacy.utils.Encoder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(
    imports = Encoder.class,
    uses = {GeneMapper.class, AlleleSpecificCopyNumberMapper.class})
public interface MutationMapper {
  MutationMapper INSTANCE = Mappers.getMapper(MutationMapper.class);

  @Mapping(
      target = "uniqueSampleKey",
      expression =
          "java( Encoder.calculateBase64(mutation.getSampleId()," + "mutation.getStudyId()) )")
  @Mapping(
      target = "uniquePatientKey",
      expression =
          "java( Encoder.calculateBase64(mutation.getPatientId(), " + "mutation.getStudyId()) )")
  @Mapping(source = "tumorSeqAllele", target = "variantAllele")
  MutationDTO toMutationDTOO(Mutation mutation);

  List<MutationDTO> toDTOs(List<Mutation> mutationList);
}
