/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.portal.service;

import java.util.List;
/**
 *
 * @author heinsz
 */

import org.mskcc.cbio.portal.model.GenePanel;
import org.mskcc.cbio.portal.model.GenePanelWithSamples;

public interface GenePanelServiceLegacy {
    List<GenePanelWithSamples> getGenePanelDataByProfileAndGenes(
        String profileId,
        List<String> genes
    );
    List<GenePanel> getGenePanelByStableId(String panelId);
    List<GenePanel> getGenePanels();
}
