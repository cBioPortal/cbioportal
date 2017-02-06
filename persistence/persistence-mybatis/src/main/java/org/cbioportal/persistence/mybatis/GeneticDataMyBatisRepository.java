package org.cbioportal.persistence.mybatis;

import java.util.List;

import org.cbioportal.model.GenesetAlteration;
import org.cbioportal.model.GeneticAlteration;
import org.cbioportal.persistence.GeneticDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class GeneticDataMyBatisRepository implements GeneticDataRepository {

    @Autowired
    private GeneticDataMapper geneticDataMapper;

    @Override
    public String getCommaSeparatedSampleIdsOfGeneticProfile(String geneticProfileId) {

        return geneticDataMapper.getCommaSeparatedSampleIdsOfGeneticProfile(geneticProfileId);
    }

    @Override
    public List<GeneticAlteration> getGeneticAlterations(String geneticProfileId, List<Integer> entrezGeneIds, 
                                                         String projection) {

        return geneticDataMapper.getGeneticAlterations(geneticProfileId, entrezGeneIds, projection);
    }

	@Override
	public List<GenesetAlteration> getGenesetAlterations(String geneticProfileId, List<String> genesetIds,
			String projection) {
		return geneticDataMapper.getGenesetAlterations(geneticProfileId, genesetIds, projection);
	}
}
