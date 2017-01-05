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

import org.cbioportal.model.GenePanel;
import org.cbioportal.model.GenePanelWithSamples;
import java.util.List;
import java.util.Map;

public interface GenePanelService {
    List<GenePanelWithSamples> getGenePanelDataByProfileAndGenes(String profileId, List<String> genes);
    List<GenePanel> getGenePanelByStableId(String panelId);
    List<GenePanel> getGenePanels();
}
