package org.cbioportal.service.impl;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.cbioportal.model.GenePanel;
import org.cbioportal.model.GenePanelData;
import org.cbioportal.model.GenePanelToGene;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.persistence.GenePanelRepository;
import org.cbioportal.service.GenePanelService;
import org.cbioportal.service.exception.GenePanelNotFoundException;
import org.cbioportal.service.exception.MolecularProfileNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GenePanelServiceImpl implements GenePanelService {
    @Autowired
    private GenePanelRepository genePanelRepository;

    @Override
    public List<GenePanel> getAllGenePanels(String projection, Integer pageSize, Integer pageNumber, String sortBy, 
                                            String direction) {
        
        List<GenePanel> genePanels = genePanelRepository.getAllGenePanels(projection, pageSize, pageNumber, sortBy, 
            direction);

        if (projection.equals("DETAILED")) {

            List<GenePanelToGene> genePanelToGeneList = genePanelRepository.getGenesOfPanels(genePanels
                .stream().map(GenePanel::getStableId).collect(Collectors.toList()));

            genePanels.forEach(g -> g.setGenes(genePanelToGeneList.stream().filter(p -> p.getGenePanelId()
                .equals(g.getStableId())).collect(Collectors.toList())));
        }
        
        return genePanels;
    }

    @Override
    public BaseMeta getMetaGenePanels() {
        
        return genePanelRepository.getMetaGenePanels();
    }

    @Override
    public GenePanel getGenePanel(String genePanelId) throws GenePanelNotFoundException {

        GenePanel genePanel = genePanelRepository.getGenePanel(genePanelId);
        if (genePanel == null) {
            throw new GenePanelNotFoundException(genePanelId);
        }
        
        genePanel.setGenes(genePanelRepository.getGenesOfPanels(Arrays.asList(genePanelId)));
        return genePanel;
    }

    @Override
	public List<GenePanel> fetchGenePanels(List<String> genePanelIds, String projection) {

        List<GenePanel> genePanels = genePanelRepository.fetchGenePanels(genePanelIds, projection);
        
        if (projection.equals("DETAILED")) {

            List<GenePanelToGene> genePanelToGeneList = genePanelRepository.getGenesOfPanels(genePanels
                .stream().map(GenePanel::getStableId).collect(Collectors.toList()));

            genePanels.forEach(g -> g.setGenes(genePanelToGeneList.stream().filter(p -> p.getGenePanelId()
                .equals(g.getStableId())).collect(Collectors.toList())));
        }

        return genePanels;
	}
    
    @Override
    public List<GenePanelData> getGenePanelData(String molecularProfileId, String sampleListId) 
        throws MolecularProfileNotFoundException {

        return genePanelRepository.getGenePanelDataBySampleListId(molecularProfileId, sampleListId);
    }

    @Override
    public List<GenePanelData> fetchGenePanelData(String molecularProfileId, List<String> sampleIds) 
        throws MolecularProfileNotFoundException {

        return genePanelRepository.fetchGenePanelData(molecularProfileId, sampleIds);
    }

    @Override
	public List<GenePanelData> fetchGenePanelDataInMultipleMolecularProfiles(List<String> molecularProfileIds,
			List<String> sampleIds) {

        return genePanelRepository.fetchGenePanelDataInMultipleMolecularProfiles(molecularProfileIds, sampleIds);
	}

    @Override
    public List<GenePanelData> fetchGenePanelDataInMultipleMolecularProfilesByPatientIds(
            List<String> molecularProfileIds, List<String> patientIds) {

        return genePanelRepository
                .fetchGenePanelDataInMultipleMolecularProfilesByPatientIds(molecularProfileIds, patientIds);
    }
}
