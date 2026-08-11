package org.cbioportal.application.rest.mapper;

import java.util.List;
import org.cbioportal.application.rest.response.CopyNumberSegDTO;
import org.cbioportal.legacy.model.CopyNumberSeg;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface CopyNumberSegMapper {
  CopyNumberSegMapper INSTANCE = Mappers.getMapper(CopyNumberSegMapper.class);

  @Mapping(target = "studyId", source = "cancerStudyIdentifier")
  @Mapping(target = "sampleId", source = "sampleStableId")
  @Mapping(target = "chromosome", source = "chr")
  @Mapping(target = "numberOfProbes", source = "numProbes")
  CopyNumberSegDTO toDto(CopyNumberSeg copyNumberSeg);

  List<CopyNumberSegDTO> toDtos(List<CopyNumberSeg> copyNumberSegs);
}
