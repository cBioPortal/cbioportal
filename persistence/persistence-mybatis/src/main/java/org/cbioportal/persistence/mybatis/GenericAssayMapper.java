package org.cbioportal.persistence.mybatis;

import java.util.HashMap;
import java.util.List;

import org.cbioportal.model.meta.GenericAssayMeta;

public interface GenericAssayMapper {

    List<GenericAssayMeta> getGenericAssayMeta(List<String> stableIds);

    int getGeneticEntityIdByStableId(String stableId);

    List<HashMap<String, String>> getGenericAssayMetaPropertiesMap(int geneticEntityId);

}