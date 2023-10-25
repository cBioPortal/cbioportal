package org.cbioportal.service;

import org.cbioportal.model.GenePanel;
import org.cbioportal.model.GenePanelData;
import org.cbioportal.model.MolecularProfileCaseIdentifier;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.service.exception.GenePanelNotFoundException;
import org.cbioportal.service.exception.MolecularProfileNotFoundException;
import org.springframework.cache.annotation.Cacheable;

import java.util.List;
import java.util.Set;

public interface GenePanelService {
    
    List<GenePanel> getAllGenePanels(String projection, Integer pageSize, Integer pageNumber, String sortBy, 
                                     String direction);
    
    BaseMeta getMetaGenePanels();

    GenePanel getGenePanel(String genePanelId) throws GenePanelNotFoundException;
    
    List<GenePanelData> getGenePanelData(String molecularProfileId, String sampleListId) 
        throws MolecularProfileNotFoundException;

    List<GenePanelData> fetchGenePanelData(String molecularProfileId, List<String> sampleIds) 
        throws MolecularProfileNotFoundException;

    @Cacheable(cacheResolver = "staticRepositoryCacheOneResolver", condition = "@cacheEnabledConfig.getEnabled()")
    List<GenePanelData> fetchGenePanelDataByMolecularProfileIds(Set<String> molecularProfileIds);

    @Cacheable(cacheResolver = "staticRepositoryCacheOneResolver", condition = "@cacheEnabledConfig.getEnabled()")
    List<GenePanelData> fetchGenePanelDataInMultipleMolecularProfiles(List<MolecularProfileCaseIdentifier> molecularProfileSampleIdentifiers);

    @Cacheable(cacheResolver = "staticRepositoryCacheOneResolver", condition = "@cacheEnabledConfig.getEnabled()")
    List<GenePanelData> fetchGenePanelDataInMultipleMolecularProfilesByPatientIds(List<MolecularProfileCaseIdentifier> molecularProfileSampleIdentifiers);

	List<GenePanel> fetchGenePanels(List<String> genePanelIds, String projection);
}
