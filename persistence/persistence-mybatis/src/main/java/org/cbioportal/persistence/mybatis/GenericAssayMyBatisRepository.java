package org.cbioportal.persistence.mybatis;

import org.cbioportal.model.GeneMolecularAlteration;
import org.cbioportal.model.GenesetMolecularAlteration;
import org.cbioportal.model.meta.GenericAssayMeta;
import org.cbioportal.persistence.GenericAssayRepository;
import org.cbioportal.persistence.MolecularDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Repository
public class GenericAssayMyBatisRepository implements GenericAssayRepository {

    @Autowired
    private GenericAssayMapper genericAssayMapper;

    private static final Logger LOG = LoggerFactory.getLogger(GenericAssayMyBatisRepository.class);

    @Override
    public List<GenericAssayMeta> getGenericAssayMeta(List<String> stableIds) {

        return genericAssayMapper.getGenericAssayMeta(stableIds);
    }

    @Override
    public List<String> getGenericAssayStableIdsByMolecularIds(List<String> molecularProfileIds) {
        
        List<Integer> molecularProfileInternalIds = genericAssayMapper.getMolecularProfileInternalIdsByMolecularProfileIds(molecularProfileIds);
        if (molecularProfileInternalIds.size() > 0) {
            List<Integer> geneticEntityIds = genericAssayMapper.getGeneticEntityIdsByMolecularProfileInternalIds(molecularProfileInternalIds);
            if (geneticEntityIds.size() > 0) {
                // return result
                return genericAssayMapper.getGenericAssayStableIdsByGeneticEntityIds(geneticEntityIds);
            } else {
                LOG.error("Returned an Empty list. Cannot find accociate entity ids for molecular profiles: " + molecularProfileIds.toString());
            }
        } else {
            LOG.error("Returned an Empty list. Cannot find internal ids for molecular profiles: " + molecularProfileIds.toString());
        }
        // log error and return empty list if something went wrong
        return new ArrayList<String>();
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