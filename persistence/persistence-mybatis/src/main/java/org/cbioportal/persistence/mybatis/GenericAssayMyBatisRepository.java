package org.cbioportal.persistence.mybatis;

import org.cbioportal.model.GeneMolecularAlteration;
import org.cbioportal.model.GenesetMolecularAlteration;
import org.cbioportal.model.meta.GenericAssayMeta;
import org.cbioportal.persistence.GenericAssayRepository;
import org.cbioportal.persistence.MolecularDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

@Repository
public class GenericAssayMyBatisRepository implements GenericAssayRepository {

    @Autowired
    private GenericAssayMapper genericAssayMapper;

    @Override
    public List<GenericAssayMeta> getGenericAssayMetaByGenericAssayType(String genericAssayType, String projection,
            Integer limit, Integer offset, String sortBy, String direction) {

        return genericAssayMapper.getGenericAssayMetaByGenericAssayType(genericAssayType, projection, limit, offset, sortBy, direction);
    }

    @Override
    public List<GenericAssayMeta> getGenericAssayMeta(List<String> stableIds) {

        return genericAssayMapper.getGenericAssayMeta(stableIds);
    }

    @Override
    public List<String> getGenericAssayStableIdsByMolecularIds(List<String> molecularProfileIds) {
        
        List<Integer> molecularProfileInternalIds = genericAssayMapper.getMolecularProfileInternalIdsByMolecularProfileIds(molecularProfileIds);
        List<Integer> geneticEntityIds = genericAssayMapper.getGeneticEntityIdsByMolecularProfileInternalIds(molecularProfileInternalIds);
        return genericAssayMapper.getGenericAssayStableIdsByGeneticEntityIds(geneticEntityIds);
    }

    @Override
    public int getGeneticEntityIdByStableId(String stableId) {

        return genericAssayMapper.getGeneticEntityIdByStableId(stableId);
    }

    @Override
    public List<HashMap<String, String>> getGenericAssayMetaPropertiesMap(int geneticEntityId) {

        return genericAssayMapper.getGenericAssayMetaPropertiesMap(geneticEntityId);
    }
}