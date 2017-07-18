package org.cbioportal.service;

import org.cbioportal.model.GenePanel;
import org.cbioportal.model.GenePanelData;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.service.exception.GenePanelNotFoundException;
import org.cbioportal.service.exception.GeneticProfileNotFoundException;

import java.util.List;

public interface GenePanelService {
    
    List<GenePanel> getAllGenePanels(String projection, Integer pageSize, Integer pageNumber, String sortBy, 
                                     String direction);
    
    BaseMeta getMetaGenePanels();

    GenePanel getGenePanel(String genePanelId) throws GenePanelNotFoundException;
    
    List<GenePanelData> getGenePanelData(String geneticProfileId, String sampleListId, List<Integer> entrezGeneIds) throws GeneticProfileNotFoundException;

    List<GenePanelData> fetchGenePanelData(String geneticProfileId, List<String> sampleIds, 
                                           List<Integer> entrezGeneIds) throws GeneticProfileNotFoundException;
}
