package org.cbioportal.application.rest.mapper;

import java.util.List;
import org.cbioportal.application.rest.response.SampleListDTO;
import org.cbioportal.legacy.model.SampleList;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(uses = CancerStudyMapper.class)
public interface SampleListMapper {
  SampleListMapper INSTANCE = Mappers.getMapper(SampleListMapper.class);

  @Mapping(target = "sampleListId", source = "stableId")
  @Mapping(target = "studyId", source = "cancerStudyIdentifier")
  @Mapping(target = "study", source = "cancerStudy")
  SampleListDTO toDto(SampleList sampleList);

  List<SampleListDTO> toDtos(List<SampleList> sampleLists);
}
