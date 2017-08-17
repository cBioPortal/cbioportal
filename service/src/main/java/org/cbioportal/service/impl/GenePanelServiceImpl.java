package org.cbioportal.service.impl;

import org.cbioportal.model.GenePanel;
import org.cbioportal.model.GenePanelData;
import org.cbioportal.model.GenePanelToGene;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.persistence.GenePanelRepository;
import org.cbioportal.service.GenePanelService;
import org.cbioportal.service.GeneticProfileService;
import org.cbioportal.service.exception.GenePanelNotFoundException;
import org.cbioportal.service.exception.GeneticProfileNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GenePanelServiceImpl implements GenePanelService {
    
    @Autowired
    private GenePanelRepository genePanelRepository;
    @Autowired
    private GeneticProfileService geneticProfileService;

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
    @PreAuthorize("hasPermission(#geneticProfileId, 'GeneticProfile', 'read')")
    public List<GenePanelData> getGenePanelData(String geneticProfileId, String sampleListId, 
                                                List<Integer> entrezGeneIds) throws GeneticProfileNotFoundException {

        geneticProfileService.getGeneticProfile(geneticProfileId);
        
        List<GenePanelData> genePanelDataList = genePanelRepository.getGenePanelData(geneticProfileId, sampleListId);
        assignGenesOfPanels(genePanelDataList, entrezGeneIds);
        
        return genePanelDataList;
    }

    @Override
    @PreAuthorize("hasPermission(#geneticProfileId, 'GeneticProfile', 'read')")
    public List<GenePanelData> fetchGenePanelData(String geneticProfileId, List<String> sampleIds, 
                                                  List<Integer> entrezGeneIds) throws GeneticProfileNotFoundException {

        geneticProfileService.getGeneticProfile(geneticProfileId);

        List<GenePanelData> genePanelDataList = genePanelRepository.fetchGenePanelData(geneticProfileId, sampleIds);
        assignGenesOfPanels(genePanelDataList, entrezGeneIds);

        return genePanelDataList;
    }

    private void assignGenesOfPanels(List<GenePanelData> genePanelDataList, List<Integer> entrezGeneIds) {
        
        List<GenePanelToGene> genePanelToGeneList = genePanelRepository.getGenesOfPanels(genePanelDataList.stream()
            .map(GenePanelData::getGenePanelId).collect(Collectors.toList()));

        genePanelDataList.forEach(g1 -> g1.setEntrezGeneIds(genePanelToGeneList.stream().filter(g2 ->
            g2.getGenePanelId().equals(g1.getGenePanelId())).filter(g2 -> entrezGeneIds.contains(g2.getEntrezGeneId()))
            .map(GenePanelToGene::getEntrezGeneId).collect(Collectors.toList())));
    }
}
