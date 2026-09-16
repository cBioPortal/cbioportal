package org.cbioportal.application.rest.mapper;

import java.util.List;
import org.cbioportal.application.rest.response.GenesetHierarchyInfoDTO;
import org.cbioportal.legacy.model.GenesetHierarchyInfo;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(uses = GenesetMapper.class)
public interface GenesetHierarchyInfoMapper {
  GenesetHierarchyInfoMapper INSTANCE = Mappers.getMapper(GenesetHierarchyInfoMapper.class);

  GenesetHierarchyInfoDTO toDto(GenesetHierarchyInfo genesetHierarchyInfo);

  List<GenesetHierarchyInfoDTO> toDtos(List<GenesetHierarchyInfo> genesetHierarchyInfos);
}
