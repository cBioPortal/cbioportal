package org.cbioportal.persistence;

import org.cbioportal.model.GenePanel;
import org.cbioportal.model.GenePanelData;
import org.cbioportal.model.GenePanelToGene;
import org.cbioportal.model.meta.BaseMeta;

import java.util.List;

public interface GenePanelRepository {

    List<GenePanel> getAllGenePanels(String projection, Integer pageSize, Integer pageNumber, String sortBy,
                                     String direction);

    BaseMeta getMetaGenePanels();

    GenePanel getGenePanel(String genePanelId);

    List<GenePanelData> getGenePanelData(String geneticProfileId, String sampleListId);

    List<GenePanelData> fetchGenePanelData(String geneticProfileId, List<String> sampleIds);
    
    List<GenePanelToGene> getGenesOfPanels(List<String> genePanelIds);
}
