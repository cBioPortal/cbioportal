package org.cbioportal.persistence.mybatis;

import org.cbioportal.model.GenePanel;
import org.cbioportal.model.GenePanelData;
import org.cbioportal.model.GenePanelToGene;
import org.cbioportal.model.MolecularProfileCaseIdentifier;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.persistence.GenePanelRepository;
import org.cbioportal.persistence.PersistenceConstants;
import org.cbioportal.persistence.mybatis.util.PaginationCalculator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Set;

@Repository
public class GenePanelMyBatisRepository implements GenePanelRepository {
    
    @Autowired
    private GenePanelMapper genePanelMapper;

    @Override
    public List<GenePanel> getAllGenePanels(String projection, Integer pageSize, Integer pageNumber, String sortBy, 
                                            String direction) {
        return genePanelMapper.getAllGenePanels(projection, pageSize, PaginationCalculator.offset(pageSize, pageNumber),
            sortBy, direction);
    }

    @Override
    public BaseMeta getMetaGenePanels() {
        return genePanelMapper.getMetaGenePanels();
    }

    @Override
    public GenePanel getGenePanel(String genePanelId) {
        return genePanelMapper.getGenePanel(genePanelId, PersistenceConstants.DETAILED_PROJECTION);
    }

    @Override
	public List<GenePanel> fetchGenePanels(List<String> genePanelIds, String projection) {
        return genePanelMapper.fetchGenePanels(genePanelIds, projection);
	}

    @Override
    public List<GenePanelData> getGenePanelDataBySampleListId(String molecularProfileId, String sampleListId) {
        return genePanelMapper.getGenePanelDataBySampleListId(molecularProfileId, sampleListId);
    }

    @Override
    public List<GenePanelData> fetchGenePanelData(String molecularProfileId, List<String> sampleIds) {
        return genePanelMapper.getGenePanelDataBySampleIds(molecularProfileId, sampleIds);
    }

    @Override
    public List<GenePanelData> fetchGenePanelDataByMolecularProfileIds(Set<String> molecularProfileIds) {
        return genePanelMapper.fetchGenePanelDataByMolecularProfileIds(molecularProfileIds);
    }

    @Override
    public List<GenePanelData> fetchGenePanelDataByMolecularProfileId(String molecularProfileId) {
        return genePanelMapper.fetchGenePanelDataByMolecularProfileIds(Collections.singleton(molecularProfileId));
    }

    @Override
    public List<GenePanelData> fetchGenePanelDataInMultipleMolecularProfiles(List<MolecularProfileCaseIdentifier> molecularProfileSampleIdentifiers) {
        return genePanelMapper.fetchGenePanelDataInMultipleMolecularProfiles(molecularProfileSampleIdentifiers);
    }

    @Override
    public List<GenePanelData> fetchGenePanelDataInMultipleMolecularProfilesByPatientIds(List<MolecularProfileCaseIdentifier> molecularProfileSampleIdentifiers) {
        return genePanelMapper.fetchGenePanelDataInMultipleMolecularProfilesByPatientIds(molecularProfileSampleIdentifiers);
    }

    @Override
    public List<GenePanelToGene> getGenesOfPanels(List<String> genePanelIds) {
        return genePanelMapper.getGenesOfPanels(genePanelIds);
    }
}
