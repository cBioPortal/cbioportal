package org.cbioportal.persistence.mybatis;

import java.util.HashMap;
import java.util.List;

import org.cbioportal.model.meta.GenericAssayMeta;
import org.cbioportal.model.GenericAssayAdditionalProperty;

public interface GenericAssayMapper {

    List<GenericAssayMeta> getGenericAssayMeta(List<String> stableIds);

    List<GenericAssayAdditionalProperty> getGenericAssayAdditionalproperties(List<String> stableIds);

    List<Integer> getMolecularProfileInternalIdsByMolecularProfileIds(List<String> molecularProfileIds);

    List<Integer> getGeneticEntityIdsByMolecularProfileInternalIds(List<Integer> molecularProfileInternalIds);
    
    List<String> getGenericAssayStableIdsByGeneticEntityIds(List<Integer> geneticEntityIds);

}