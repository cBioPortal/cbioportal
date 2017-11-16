package org.cbioportal.persistence.mybatis;

import org.cbioportal.model.MolecularProfile;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.persistence.MolecularProfileRepository;
import org.cbioportal.persistence.PersistenceConstants;
import org.cbioportal.persistence.mybatis.util.OffsetCalculator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class MolecularProfileMyBatisRepository implements MolecularProfileRepository {

    @Autowired
    private MolecularProfileMapper molecularProfileMapper;
    @Autowired
    private OffsetCalculator offsetCalculator;

    @Override
    public List<MolecularProfile> getAllMolecularProfiles(String projection, Integer pageSize, Integer pageNumber,
                                                          String sortBy, String direction) {

        return molecularProfileMapper.getAllMolecularProfiles(null, projection, pageSize,
                offsetCalculator.calculate(pageSize, pageNumber), sortBy, direction);
    }

    @Override
    public BaseMeta getMetaMolecularProfiles() {

        return molecularProfileMapper.getMetaMolecularProfiles(null);
    }

    @Override
    public MolecularProfile getMolecularProfile(String molecularProfileId) {
        return molecularProfileMapper.getMolecularProfile(molecularProfileId, PersistenceConstants.DETAILED_PROJECTION);
    }

    @Override
    public List<MolecularProfile> getAllMolecularProfilesInStudy(String studyId, String projection, Integer pageSize,
                                                                 Integer pageNumber, String sortBy, String direction) {

        return molecularProfileMapper.getAllMolecularProfiles(studyId, projection, pageSize,
                offsetCalculator.calculate(pageSize, pageNumber), sortBy, direction);
    }

    @Override
    public BaseMeta getMetaMolecularProfilesInStudy(String studyId) {

        return molecularProfileMapper.getMetaMolecularProfiles(studyId);
    }

	@Override
	public List<MolecularProfile> getMolecularProfilesReferredBy(String referringMolecularProfileId) {

		return molecularProfileMapper.getMolecularProfilesReferredBy(referringMolecularProfileId, 
            PersistenceConstants.DETAILED_PROJECTION);
	}

	@Override
	public List<MolecularProfile> getMolecularProfilesReferringTo(String referredMolecularProfileId) {
		
		return molecularProfileMapper.getMolecularProfilesReferringTo(referredMolecularProfileId, 
            PersistenceConstants.DETAILED_PROJECTION);
	}
}
