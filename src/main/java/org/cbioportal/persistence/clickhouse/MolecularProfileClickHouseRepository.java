package org.cbioportal.persistence.clickhouse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.cbioportal.model.MolecularProfile;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.persistence.MolecularProfileRepository;
import org.cbioportal.persistence.PersistenceConstants;
import org.cbioportal.persistence.clickhouse.mapper.MolecularProfileMapper;
import org.cbioportal.persistence.clickhouse.util.OffsetCalculator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("clickhouse")
public class MolecularProfileClickHouseRepository implements MolecularProfileRepository {
	
	@Autowired
	MolecularProfileMapper molecularProfileMapper;

	@Override
	public List<MolecularProfile> getAllMolecularProfiles(String projection, Integer pageSize, Integer pageNumber,
			String sortBy, String direction) {
		return molecularProfileMapper.getAllMolecularProfilesInStudies(null, projection, pageSize,
                OffsetCalculator.calculate(pageSize, pageNumber), sortBy, direction);
	}

	@Override
	public BaseMeta getMetaMolecularProfiles() {
		return molecularProfileMapper.getMetaMolecularProfilesInStudies(null);
	}

	@Override
	public MolecularProfile getMolecularProfile(String molecularProfileId) {
		return molecularProfileMapper.getMolecularProfile(molecularProfileId, PersistenceConstants.DETAILED_PROJECTION);
	}

	@Override
	public List<MolecularProfile> getMolecularProfiles(Set<String> molecularProfileIds, String projection) {
		 return molecularProfileMapper.getMolecularProfiles(molecularProfileIds, projection);
	}

	@Override
	public BaseMeta getMetaMolecularProfiles(Set<String> molecularProfileIds) {
		return molecularProfileMapper.getMetaMolecularProfiles(molecularProfileIds);
	}

	@Override
	public List<MolecularProfile> getAllMolecularProfilesInStudy(String studyId, String projection, Integer pageSize,
			Integer pageNumber, String sortBy, String direction) {

		return molecularProfileMapper.getAllMolecularProfilesInStudies(Arrays.asList(studyId), projection, pageSize,
                OffsetCalculator.calculate(pageSize, pageNumber), sortBy, direction);
	}

	@Override
	public BaseMeta getMetaMolecularProfilesInStudy(String studyId) {
		return molecularProfileMapper.getMetaMolecularProfilesInStudies(Arrays.asList(studyId));
	}

	@Override
	public List<MolecularProfile> getMolecularProfilesInStudies(List<String> studyIds, String projection) {
		return molecularProfileMapper.getAllMolecularProfilesInStudies(studyIds, projection, 0, 0, null, null);
	}

	@Override
	public BaseMeta getMetaMolecularProfilesInStudies(List<String> studyIds) {
		return molecularProfileMapper.getMetaMolecularProfilesInStudies(studyIds);
	}

	@Override
	public List<MolecularProfile> getMolecularProfilesReferredBy(String referringMolecularProfileId) {
		// TODO Auto-generated method stub
		return new ArrayList<MolecularProfile>();
	}

	@Override
	public List<MolecularProfile> getMolecularProfilesReferringTo(String referredMolecularProfileId) {
		// TODO Auto-generated method stub
		return new ArrayList<MolecularProfile>();
	}

}
