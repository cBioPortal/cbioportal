package org.cbioportal.application.rest.mapper;

import java.util.List;
import org.cbioportal.application.rest.response.TypeOfCancerDTO;
import org.cbioportal.legacy.model.TypeOfCancer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface TypeOfCancerMapper {
  TypeOfCancerMapper INSTANCE = Mappers.getMapper(TypeOfCancerMapper.class);

  @Mapping(target = "cancerTypeId", source = "typeOfCancerId")
  TypeOfCancerDTO toDto(TypeOfCancer typeOfCancer);

  @Mapping(target = "cancerTypeId", source = "id")
  TypeOfCancerDTO toDto(org.cbioportal.domain.cancerstudy.TypeOfCancer typeOfCancer);

  List<TypeOfCancerDTO> toDtos(List<TypeOfCancer> typeOfCancers);
}
