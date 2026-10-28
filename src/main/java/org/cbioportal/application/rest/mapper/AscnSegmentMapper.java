package org.cbioportal.application.rest.mapper;

import java.util.List;
import org.cbioportal.application.rest.response.AscnSegmentDTO;
import org.cbioportal.domain.ascn.AscnSegment;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface AscnSegmentMapper {
  AscnSegmentMapper INSTANCE = Mappers.getMapper(AscnSegmentMapper.class);

  AscnSegmentDTO toDto(AscnSegment ascnSegment);

  List<AscnSegmentDTO> toDtos(List<AscnSegment> ascnSegments);
}
