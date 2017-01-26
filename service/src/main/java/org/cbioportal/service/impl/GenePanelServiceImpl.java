/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cbioportal.service.impl;

/**
 *
 * @author heinsz
 */

import org.cbioportal.model.GenePanel;
import org.cbioportal.model.Gene;
import org.cbioportal.model.GenePanelWithSamples;
import org.cbioportal.persistence.GenePanelRepository;
import org.cbioportal.service.GenePanelService;
import java.util.List;
import java.util.ArrayList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GenePanelServiceImpl implements GenePanelService {
    
    @Autowired
    private GenePanelRepository genePanelRepository;

    @Override
    public List<GenePanelWithSamples> getGenePanelDataByProfileAndGenes(String profileId, List<String> submittedGenes) {
        List<GenePanelWithSamples> genePanels =  genePanelRepository.getGenePanelsByProfile(profileId);
        for (GenePanelWithSamples genePanel : genePanels) {
            List<Gene> genes = genePanel.getGenes();
            List<Gene> genesToSet = new ArrayList<>();
            for (Gene gene : genes) {
                if (submittedGenes.contains(gene.getHugoGeneSymbol())) {
                    genesToSet.add(gene);
                }                
            }
            genePanel.setGenes(genesToSet);
        }
        return genePanels;
    }
    
    @Override
    public List<GenePanel> getGenePanelByStableId(String panelId) {
        // TODO: create proper mixin to not expose the internal id to external user.
        List<GenePanel> genePanels = genePanelRepository.getGenePanelByStableId(panelId);
        for (GenePanel genePanel : genePanels) {
            genePanel.setInternalId(null);
        }
        return genePanels;
    }
    
    @Override
    public List<GenePanel> getGenePanels() {
        return genePanelRepository.getGenePanels();
    }
}
