package org.cbioportal.legacy.persistence.mybatis;

import java.util.List;
import org.cbioportal.legacy.model.GenericAssayAdditionalProperty;
import org.cbioportal.legacy.model.meta.GenericAssayMeta;

public interface GenericAssayMapper {

  List<GenericAssayMeta> getGenericAssayMeta(List<String> stableIds);

  List<GenericAssayAdditionalProperty> getGenericAssayAdditionalproperties(List<String> stableIds);

  List<Integer> getMolecularProfileInternalIdsByMolecularProfileIds(
      List<String> molecularProfileIds);

  List<Integer> getGeneticEntityIdsByMolecularProfileInternalIds(
      List<Integer> molecularProfileInternalIds);

  List<String> getGenericAssayStableIdsByGeneticEntityIds(List<Integer> geneticEntityIds);
}
