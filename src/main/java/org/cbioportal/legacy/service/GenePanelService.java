package org.cbioportal.legacy.service;

import org.cbioportal.legacy.model.GenePanel;
import org.cbioportal.legacy.model.GenePanelData;
import org.cbioportal.legacy.model.MolecularProfileCaseIdentifier;
import org.cbioportal.legacy.model.meta.BaseMeta;
import org.cbioportal.legacy.service.exception.GenePanelNotFoundException;
import org.cbioportal.legacy.service.exception.MolecularProfileNotFoundException;
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

    /**
     * Finds Entrez Gene IDs from the input collection that are present
     * in the gene_panel_list table (i.e., associated with at least one gene panel).
     * This method is intended to be used by other services to determine which genes
     * out of a given set have panel associations.
     *
     * @param geneIdsToCheck Collection of Entrez Gene IDs to check.
     * @return A Set of Entrez Gene IDs from the input collection that were found
     * associated with at least one gene panel in the database.
     * Returns an empty set if the input is null/empty or no matches are found.
     */
    Set<Integer> findGeneIdsAssociatedWithAnyPanel(Set<Integer> geneIdsToCheck);
}
