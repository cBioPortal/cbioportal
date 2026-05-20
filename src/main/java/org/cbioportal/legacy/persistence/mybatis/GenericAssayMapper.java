package org.cbioportal.legacy.persistence.mybatis;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.cbioportal.legacy.model.GenericAssayAdditionalProperty;
import org.cbioportal.legacy.model.meta.GenericAssayMeta;

public interface GenericAssayMapper {

  List<GenericAssayMeta> getGenericAssayMeta(List<String> stableIds);

  List<GenericAssayMeta> getPageableGenericAssayMeta(
      @Param("stableIds") List<String> stableIds,
      @Param("keyword") String keyword,
      @Param("limit") Integer limit,
      @Param("offset") Integer offset,
      @Param("sortBy") String sortBy,
      @Param("direction") String direction);

  List<GenericAssayAdditionalProperty> getGenericAssayAdditionalproperties(List<String> stableIds);

  List<Integer> getMolecularProfileInternalIdsByMolecularProfileIds(
      List<String> molecularProfileIds);

  List<Integer> getGeneticEntityIdsByMolecularProfileInternalIds(
      List<Integer> molecularProfileInternalIds);

  List<String> getGenericAssayStableIdsByGeneticEntityIds(List<Integer> geneticEntityIds);
}
