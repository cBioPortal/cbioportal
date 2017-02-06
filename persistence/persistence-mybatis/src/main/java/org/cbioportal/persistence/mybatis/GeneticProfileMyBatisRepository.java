package org.cbioportal.persistence.mybatis;

import org.cbioportal.model.GeneticProfile;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.persistence.GeneticProfileRepository;
import org.cbioportal.persistence.PersistenceConstants;
import org.cbioportal.persistence.mybatis.util.OffsetCalculator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class GeneticProfileMyBatisRepository implements GeneticProfileRepository {

    @Autowired
    private GeneticProfileMapper geneticProfileMapper;
    @Autowired
    private OffsetCalculator offsetCalculator;

    @Override
    public List<GeneticProfile> getAllGeneticProfiles(String projection, Integer pageSize, Integer pageNumber,
                                                      String sortBy, String direction) {

        return geneticProfileMapper.getAllGeneticProfiles(null, projection, pageSize,
                offsetCalculator.calculate(pageSize, pageNumber), sortBy, direction);
    }

    @Override
    public BaseMeta getMetaGeneticProfiles() {

        return geneticProfileMapper.getMetaGeneticProfiles(null);
    }

    @Override
    public GeneticProfile getGeneticProfile(String geneticProfileId) {
        return geneticProfileMapper.getGeneticProfile(geneticProfileId, PersistenceConstants.DETAILED_PROJECTION);
    }

    @Override
    public List<GeneticProfile> getAllGeneticProfilesInStudy(String studyId, String projection, Integer pageSize,
                                                             Integer pageNumber, String sortBy, String direction) {

        return geneticProfileMapper.getAllGeneticProfiles(studyId, projection, pageSize,
                offsetCalculator.calculate(pageSize, pageNumber), sortBy, direction);
    }

    @Override
    public BaseMeta getMetaGeneticProfilesInStudy(String studyId) {

        return geneticProfileMapper.getMetaGeneticProfiles(studyId);
    }

	@Override
	public List<GeneticProfile> getGeneticProfilesReferredBy(String referringGeneticProfileId) {

		return geneticProfileMapper.getGeneticProfilesReferredBy(referringGeneticProfileId, PersistenceConstants.DETAILED_PROJECTION);
	}

	@Override
	public List<GeneticProfile> getGeneticProfilesReferringTo(String referredGeneticProfileId) {
		
		return geneticProfileMapper.getGeneticProfilesReferringTo(referredGeneticProfileId, PersistenceConstants.DETAILED_PROJECTION);
	}
}
