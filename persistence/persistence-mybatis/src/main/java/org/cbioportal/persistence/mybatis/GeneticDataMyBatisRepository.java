package org.cbioportal.persistence.mybatis;

import org.cbioportal.model.GeneticAlteration;
import org.cbioportal.persistence.GeneticDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class GeneticDataMyBatisRepository implements GeneticDataRepository {

    @Autowired
    private GeneticDataMapper geneticDataMapper;

    @Override
    public String getCommaSeparatedSampleIdsOfGeneticProfile(String geneticProfileId) {

        return geneticDataMapper.getCommaSeparatedSampleIdsOfGeneticProfile(geneticProfileId);
    }

    @Override
    public List<GeneticAlteration> getGeneticAlterations(String geneticProfileId, List<Integer> entrezGeneIds) {

        return geneticDataMapper.getGeneticAlterations(geneticProfileId, entrezGeneIds);
    }
}
