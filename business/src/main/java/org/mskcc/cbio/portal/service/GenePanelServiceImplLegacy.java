/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.portal.service;

/**
 *
 * @author heinsz
 */

import org.mskcc.cbio.portal.model.GenePanel;
import org.cbioportal.model.Gene;
import org.mskcc.cbio.portal.model.GenePanelWithSamples;
import java.util.List;
import java.util.ArrayList;

import org.mskcc.cbio.portal.repository.GenePanelRepositoryLegacy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
public class GenePanelServiceImplLegacy implements GenePanelServiceLegacy {

    @Autowired
    private GenePanelRepositoryLegacy genePanelRepositoryLegacy;

    @Override
    @PreAuthorize("hasPermission(#profileId, 'GeneticProfileId', 'read')")
    public List<GenePanelWithSamples> getGenePanelDataByProfileAndGenes(String profileId, List<String> submittedGenes) {
        List<GenePanelWithSamples> genePanels =  genePanelRepositoryLegacy.getGenePanelsByProfile(profileId);
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
        List<GenePanel> genePanels = genePanelRepositoryLegacy.getGenePanelByStableId(panelId);
        for (GenePanel genePanel : genePanels) {
            genePanel.setInternalId(null);
        }
        return genePanels;
    }
    
    @Override
    public List<GenePanel> getGenePanels() {
        return genePanelRepositoryLegacy.getGenePanels();
    }
}
