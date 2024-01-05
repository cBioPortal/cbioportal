package org.cbioportal.persistence.clickhouse;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.cbioportal.model.GenePanel;
import org.cbioportal.model.GenePanelData;
import org.cbioportal.model.GenePanelToGene;
import org.cbioportal.model.MolecularProfileCaseIdentifier;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.persistence.GenePanelRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("clickhouse")
public class GenePanelClickHouseRepository implements GenePanelRepository {

	@Override
	public List<GenePanel> getAllGenePanels(String projection, Integer pageSize, Integer pageNumber, String sortBy,
			String direction) {
		// TODO Auto-generated method stub
		return new ArrayList<GenePanel>();
	}

	@Override
	public BaseMeta getMetaGenePanels() {
		// TODO Auto-generated method stub
		return new BaseMeta();
	}

	@Override
	public GenePanel getGenePanel(String genePanelId) {
		// TODO Auto-generated method stub
		return new GenePanel();
	}

	@Override
	public List<GenePanel> fetchGenePanels(List<String> genePanelIds, String projection) {
		// TODO Auto-generated method stub
		return new ArrayList<GenePanel>();
	}

	@Override
	public List<GenePanelData> getGenePanelDataBySampleListId(String molecularProfileId, String sampleListId) {
		// TODO Auto-generated method stub
		return new ArrayList<GenePanelData>();
	}

	@Override
	public List<GenePanelData> fetchGenePanelData(String molecularProfileId, List<String> sampleIds) {
		// TODO Auto-generated method stub
		return new ArrayList<GenePanelData>();
	}

	@Override
	public List<GenePanelData> fetchGenePanelDataByMolecularProfileIds(Set<String> molecularProfileIds) {
		// TODO Auto-generated method stub
		return new ArrayList<GenePanelData>();
	}

	@Override
	public List<GenePanelData> fetchGenePanelDataByMolecularProfileId(String molecularProfileId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<GenePanelData> fetchGenePanelDataInMultipleMolecularProfiles(
			List<MolecularProfileCaseIdentifier> molecularProfileSampleIdentifiers) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<GenePanelData> fetchGenePanelDataInMultipleMolecularProfilesByPatientIds(
			List<MolecularProfileCaseIdentifier> molecularProfileSampleIdentifiers) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<GenePanelToGene> getGenesOfPanels(List<String> genePanelIds) {
		// TODO Auto-generated method stub
		return null;
	}

}
