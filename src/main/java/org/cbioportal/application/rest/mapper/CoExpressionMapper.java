package org.cbioportal.application.rest.mapper;

import java.util.List;
import org.cbioportal.application.rest.response.CoExpressionDTO;
import org.cbioportal.legacy.model.CoExpression;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface CoExpressionMapper {
  CoExpressionMapper INSTANCE = Mappers.getMapper(CoExpressionMapper.class);

  CoExpressionDTO toDTO(CoExpression coExpression);

  List<CoExpressionDTO> toDTOs(List<CoExpression> coExpressions);
}
