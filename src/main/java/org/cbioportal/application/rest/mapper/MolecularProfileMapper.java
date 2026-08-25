package org.cbioportal.application.rest.mapper;

import java.util.List;
import org.cbioportal.application.rest.response.MolecularProfileDTO;
import org.cbioportal.legacy.model.MolecularProfile;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(uses = CancerStudyMapper.class)
public interface MolecularProfileMapper {
  MolecularProfileMapper INSTANCE = Mappers.getMapper(MolecularProfileMapper.class);

  @Mapping(target = "molecularProfileId", source = "stableId")
  @Mapping(target = "studyId", source = "cancerStudyIdentifier")
  @Mapping(target = "study", source = "cancerStudy")
  MolecularProfileDTO toDto(MolecularProfile molecularProfile);

  List<MolecularProfileDTO> toDtos(List<MolecularProfile> molecularProfiles);
}
