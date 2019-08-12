package org.cbioportal.persistence;

import org.cbioportal.model.GenePanel;
import org.cbioportal.model.GenePanelData;
import org.cbioportal.model.GenePanelToGene;
import org.cbioportal.model.meta.BaseMeta;

import org.springframework.cache.annotation.Cacheable;

import java.util.List;

public interface GenePanelRepository {

    @Cacheable("GeneralRepositoryCache")
    List<GenePanel> getAllGenePanels(String projection, Integer pageSize, Integer pageNumber, String sortBy,
                                     String direction);

    @Cacheable("GeneralRepositoryCache")
    BaseMeta getMetaGenePanels();

    @Cacheable("GeneralRepositoryCache")
    GenePanel getGenePanel(String genePanelId);

    @Cacheable("GeneralRepositoryCache")
    List<GenePanel> fetchGenePanels(List<String> genePanelIds, String projection);

    @Cacheable("GeneralRepositoryCache")
    List<GenePanelData> getGenePanelData(String molecularProfileId, String sampleListId);

    @Cacheable("GeneralRepositoryCache")
    List<GenePanelData> fetchGenePanelData(String molecularProfileId, List<String> sampleIds);

    @Cacheable("GeneralRepositoryCache")
    List<GenePanelData> fetchGenePanelDataInMultipleMolecularProfiles(List<String> molecularProfileIds, 
        List<String> sampleIds);

    @Cacheable("GeneralRepositoryCache")
    List<GenePanelToGene> getGenesOfPanels(List<String> genePanelIds);
}
