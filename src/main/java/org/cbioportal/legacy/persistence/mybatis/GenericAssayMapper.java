package org.cbioportal.legacy.persistence.mybatis;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.cbioportal.legacy.model.GenericAssayAdditionalProperty;
import org.cbioportal.legacy.model.meta.GenericAssayMeta;

public interface GenericAssayMapper {

  List<GenericAssayMeta> getGenericAssayMeta(List<String> stableIds);

  /**
   * @param propertyNames when non-null and non-empty, restricts the returned properties to these
   *     names (pushed into SQL); when null, all properties for the entities are returned.
   */
  List<GenericAssayAdditionalProperty> getGenericAssayAdditionalproperties(
      @Param("stableIds") List<String> stableIds,
      @Param("propertyNames") List<String> propertyNames);

  List<Integer> getMolecularProfileInternalIdsByMolecularProfileIds(
      List<String> molecularProfileIds);

  List<Integer> getGeneticEntityIdsByMolecularProfileInternalIds(
      List<Integer> molecularProfileInternalIds);

  List<String> getGenericAssayStableIdsByGeneticEntityIds(List<Integer> geneticEntityIds);
}
