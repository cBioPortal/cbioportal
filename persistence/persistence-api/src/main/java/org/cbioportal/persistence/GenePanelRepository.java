package org.cbioportal.persistence;

import org.cbioportal.model.GenePanel;
import org.cbioportal.model.GenePanelData;
import org.cbioportal.model.GenePanelToGene;
import org.cbioportal.model.meta.BaseMeta;

import org.springframework.cache.annotation.Cacheable;

import java.util.List;

public interface GenePanelRepository {

    @Cacheable(cacheResolver = "staticRepositoryCacheOneResolver", condition = "@cacheEnabledConfig.getEnabled()")
    List<GenePanel> getAllGenePanels(String projection, Integer pageSize, Integer pageNumber, String sortBy,
                                     String direction);

    @Cacheable(cacheResolver = "staticRepositoryCacheOneResolver", condition = "@cacheEnabledConfig.getEnabled()")
    BaseMeta getMetaGenePanels();

    @Cacheable(cacheResolver = "staticRepositoryCacheOneResolver", condition = "@cacheEnabledConfig.getEnabled()")
    GenePanel getGenePanel(String genePanelId);

    @Cacheable(cacheResolver = "staticRepositoryCacheOneResolver", condition = "@cacheEnabledConfig.getEnabled()")
    List<GenePanel> fetchGenePanels(List<String> genePanelIds, String projection);

    @Cacheable(cacheResolver = "staticRepositoryCacheOneResolver", condition = "@cacheEnabledConfig.getEnabled()")
    List<GenePanelData> getGenePanelDataBySampleListId(String molecularProfileId, String sampleListId);

    @Cacheable(cacheResolver = "staticRepositoryCacheOneResolver", condition = "@cacheEnabledConfig.getEnabled()")
    List<GenePanelData> fetchGenePanelData(String molecularProfileId, List<String> sampleIds);

    @Cacheable(cacheResolver = "staticRepositoryCacheOneResolver", condition = "@cacheEnabledConfig.getEnabled()")
    List<GenePanelData> fetchGenePanelDataInMultipleMolecularProfiles(List<String> molecularProfileIds, 
        List<String> sampleIds);

    @Cacheable(cacheResolver = "staticRepositoryCacheOneResolver", condition = "@cacheEnabledConfig.getEnabled()")
    List<GenePanelData> fetchGenePanelDataInMultipleMolecularProfilesByPatientIds(List<String> molecularProfileIds, 
        List<String> patientIds);

    @Cacheable(cacheResolver = "staticRepositoryCacheOneResolver", condition = "@cacheEnabledConfig.getEnabled()")
    List<GenePanelToGene> getGenesOfPanels(List<String> genePanelIds);
}
