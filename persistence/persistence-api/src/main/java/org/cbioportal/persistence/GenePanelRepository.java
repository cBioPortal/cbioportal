package org.cbioportal.persistence;

import org.cbioportal.model.GenePanel;
import org.cbioportal.model.GenePanelData;
import org.cbioportal.model.GenePanelToGene;
import org.cbioportal.model.meta.BaseMeta;

import org.springframework.cache.annotation.Cacheable;

import java.util.List;

public interface GenePanelRepository {

    @Cacheable("RepositoryCache")
    List<GenePanel> getAllGenePanels(String projection, Integer pageSize, Integer pageNumber, String sortBy,
                                     String direction);

    @Cacheable("RepositoryCache")
    BaseMeta getMetaGenePanels();

    @Cacheable("RepositoryCache")
    GenePanel getGenePanel(String genePanelId);

    @Cacheable("RepositoryCache")
    List<GenePanel> fetchGenePanels(List<String> genePanelIds, String projection);

    @Cacheable("RepositoryCache")
    List<GenePanelData> getGenePanelData(String molecularProfileId, String sampleListId);

    @Cacheable("RepositoryCache")
    List<GenePanelData> fetchGenePanelData(String molecularProfileId, List<String> sampleIds);

    @Cacheable("RepositoryCache")
    List<GenePanelData> fetchGenePanelDataInMultipleMolecularProfiles(List<String> molecularProfileIds, 
        List<String> sampleIds);

    @Cacheable("RepositoryCache")
    List<GenePanelToGene> getGenesOfPanels(List<String> genePanelIds);
}
