package org.cbioportal.persistence;

import org.cbioportal.model.GenePanel;
import org.cbioportal.model.GenePanelData;
import org.cbioportal.model.GenePanelToGene;
import org.cbioportal.model.meta.BaseMeta;

import org.springframework.cache.annotation.Cacheable;

import java.util.List;

public interface GenePanelRepository {

    @Cacheable(cacheNames = "GeneralRepositoryCache", condition = "@cacheEnabledConfig.getEnabled()")
    List<GenePanel> getAllGenePanels(String projection, Integer pageSize, Integer pageNumber, String sortBy,
                                     String direction);

    @Cacheable(cacheNames = "GeneralRepositoryCache", condition = "@cacheEnabledConfig.getEnabled()")
    BaseMeta getMetaGenePanels();

    @Cacheable(cacheNames = "GeneralRepositoryCache", condition = "@cacheEnabledConfig.getEnabled()")
    GenePanel getGenePanel(String genePanelId);

    @Cacheable(cacheNames = "GeneralRepositoryCache", condition = "@cacheEnabledConfig.getEnabled()")
    List<GenePanel> fetchGenePanels(List<String> genePanelIds, String projection);

    @Cacheable(cacheNames = "GeneralRepositoryCache", condition = "@cacheEnabledConfig.getEnabled()")
    List<GenePanelData> getGenePanelData(String molecularProfileId, String sampleListId);

    @Cacheable(cacheNames = "GeneralRepositoryCache", condition = "@cacheEnabledConfig.getEnabled()")
    List<GenePanelData> fetchGenePanelData(String molecularProfileId, List<String> sampleIds);

    @Cacheable(cacheNames = "GeneralRepositoryCache", condition = "@cacheEnabledConfig.getEnabled()")
    List<GenePanelData> fetchGenePanelDataInMultipleMolecularProfiles(List<String> molecularProfileIds, 
        List<String> sampleIds);

    @Cacheable(cacheNames = "GeneralRepositoryCache", condition = "@cacheEnabledConfig.getEnabled()")
    List<GenePanelToGene> getGenesOfPanels(List<String> genePanelIds);
}
