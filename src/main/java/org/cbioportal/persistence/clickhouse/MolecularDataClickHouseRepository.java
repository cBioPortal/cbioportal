package org.cbioportal.persistence.clickhouse;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.cbioportal.model.GeneMolecularAlteration;
import org.cbioportal.model.GenericAssayMolecularAlteration;
import org.cbioportal.model.GenesetMolecularAlteration;
import org.cbioportal.model.GenomicDataCount;
import org.cbioportal.model.MolecularProfileSamples;
import org.cbioportal.persistence.MolecularDataRepository;
import org.cbioportal.persistence.clickhouse.mapper.MolecularDataMapper;
import org.cbioportal.web.parameter.StudyViewFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("clickhouse")
public class MolecularDataClickHouseRepository implements MolecularDataRepository {
	
    @Autowired
    private MolecularDataMapper molecularDataMapper;

	@Override
	public MolecularProfileSamples getCommaSeparatedSampleIdsOfMolecularProfile(String molecularProfileId) {
		// TODO Auto-generated method stub
		return new MolecularProfileSamples();
	}

	@Override
	public Map<String, MolecularProfileSamples> commaSeparatedSampleIdsOfMolecularProfilesMap(
			Set<String> molecularProfileIds) {
		// TODO Auto-generated method stub
		return new HashMap<String, MolecularProfileSamples>();
	}

	@Override
	public List<GeneMolecularAlteration> getGeneMolecularAlterations(String molecularProfileId,
			List<Integer> entrezGeneIds, String projection) {
		// TODO Auto-generated method stub
		return new ArrayList<GeneMolecularAlteration>();
	}

	@Override
	public Iterable<GeneMolecularAlteration> getGeneMolecularAlterationsIterable(String molecularProfileId,
			List<Integer> entrezGeneIds, String projection) {
		// TODO Auto-generated method stub
		return (Iterable<GeneMolecularAlteration>) new ArrayList<GeneMolecularAlteration>().iterator();
	}

	@Override
	public Iterable<GeneMolecularAlteration> getGeneMolecularAlterationsIterableFast(String molecularProfileId) {
		// TODO Auto-generated method stub
		return (Iterable<GeneMolecularAlteration>) new ArrayList<GeneMolecularAlteration>().iterator();
	}

	@Override
	public List<GeneMolecularAlteration> getGeneMolecularAlterationsInMultipleMolecularProfiles(
			Set<String> molecularProfileIds, List<Integer> entrezGeneIds, String projection) {
		// TODO Auto-generated method stub
		return new ArrayList<GeneMolecularAlteration>();
	}

	@Override
	public List<GenesetMolecularAlteration> getGenesetMolecularAlterations(String molecularProfileId,
			List<String> genesetIds, String projection) {
		// TODO Auto-generated method stub
		return new ArrayList<GenesetMolecularAlteration>();
	}

	@Override
	public List<GenericAssayMolecularAlteration> getGenericAssayMolecularAlterations(String molecularProfileId,
			List<String> stableIds, String projection) {
		// TODO Auto-generated method stub
		return new ArrayList<GenericAssayMolecularAlteration>();
	}

	@Override
	public Iterable<GenericAssayMolecularAlteration> getGenericAssayMolecularAlterationsIterable(
			String molecularProfileId, List<String> stableIds, String projection) {
		// TODO Auto-generated method stub
		return (Iterable<GenericAssayMolecularAlteration>) new ArrayList<GenericAssayMolecularAlteration>().iterator();
	}

	@Override
	public List<GenomicDataCount> getMolecularProfileSampleCounts(StudyViewFilter interceptedStudyViewFilter,
			boolean singleStudyUnfiltered) {

		return molecularDataMapper.getMolecularProfileSampleCounts(interceptedStudyViewFilter, singleStudyUnfiltered);
	}

}
