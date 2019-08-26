package org.cbioportal.persistence;

import java.util.HashMap;
import java.util.List;

import org.cbioportal.model.meta.GenericAssayMeta;

public interface GenericAssayRepository {

    List<GenericAssayMeta> getGenericAssayMeta(List<String> stableIds);

    int getGeneticEntityIdByStableId(String stableId);

    List<HashMap<String, String>> getGenericAssayMetaPropertiesMap(int geneticEntityId);
}
