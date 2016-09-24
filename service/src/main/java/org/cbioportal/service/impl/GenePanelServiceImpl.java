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
import org.cbioportal.persistence.GenePanelRepository;
import org.cbioportal.service.GenePanelService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GenePanelServiceImpl implements GenePanelService {
    
    @Autowired
    private GenePanelRepository genePanelRepository;

    @Override
    public String getGenePanelBySampleIdAndProfileId(String sampleId, String profileId) {
        return genePanelRepository.getGenePanelBySampleIdAndProfileId(sampleId, profileId);
    }
    
    @Override
    public List<GenePanel> getGenePanelByStableId(String panelId) {
        return genePanelRepository.getGenePanelByStableId(panelId);
    }
    
    @Override
    public List<GenePanel> getGenePanels() {
        return genePanelRepository.getGenePanels();
    }
}
