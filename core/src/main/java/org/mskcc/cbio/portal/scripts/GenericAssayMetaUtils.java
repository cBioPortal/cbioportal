package org.mskcc.cbio.portal.scripts;

import org.cbioportal.model.EntityType;
import org.cbioportal.model.GeneticEntity;
import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.dao.DaoGeneticEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GenericAssayMetaUtils {
    public static Map<String, Integer> buildGenericAssayStableIdToEntityIdMap() {
        Map<String, Integer> genericAssayStableIdToEntityIdMap = new HashMap<>();
        try {
            List<GeneticEntity> allEntities = DaoGeneticEntity.getAllGeneticEntities();
            genericAssayStableIdToEntityIdMap = allEntities.stream()
                .filter(entityType -> EntityType.GENERIC_ASSAY.toString().equals(entityType.getEntityType()))
                .collect(Collectors.toMap(GeneticEntity::getStableId, GeneticEntity::getId));
        } catch (DaoException e) {
            e.printStackTrace();
        }
        return genericAssayStableIdToEntityIdMap;
    }
}
