package org.cbioportal.legacy.persistence.mybatis;

import org.cbioportal.legacy.model.GenePanel;
import org.cbioportal.legacy.model.GenePanelData;
import org.cbioportal.legacy.model.GenePanelToGene;
import org.cbioportal.legacy.model.MolecularProfileCaseIdentifier;
import org.cbioportal.legacy.model.meta.BaseMeta;

import java.util.List;
import java.util.Set;

public interface GenePanelMapper {

    List<GenePanel> getAllGenePanels(String projection, Integer limit, Integer offset, String sortBy,
                                     String direction);

    BaseMeta getMetaGenePanels();

    GenePanel getGenePanel(String genePanelId, String projection);

    List<GenePanel> fetchGenePanels(List<String> genePanelIds, String projection);

    List<GenePanelData> getGenePanelDataBySampleListId(String molecularProfileId, String sampleListId);

    List<GenePanelData> getGenePanelDataBySampleIds(String molecularProfileId, List<String> sampleIds);
    
    List<GenePanelData> fetchGenePanelDataByMolecularProfileIds(Set<String> molecularProfileIds);

    List<GenePanelData> fetchGenePanelDataInMultipleMolecularProfiles(List<MolecularProfileCaseIdentifier> molecularProfileSampleIdentifiers);

    List<GenePanelData> fetchGenePanelDataInMultipleMolecularProfilesByPatientIds(List<MolecularProfileCaseIdentifier> molecularProfileSampleIdentifiers);

    List<GenePanelToGene> getGenesOfPanels(List<String> genePanelIds);

    /**
     * Finds Entrez Gene IDs from the input collection that exist in the gene_panel_list table.
     * @param geneIdsToCheck Collection of Entrez Gene IDs to check.
     * @return A Set (or List) of Entrez Gene IDs found in gene_panel_list.
     */
    Set<Integer> findGeneIdsPresentInGenePanelList(Set<Integer> geneIdsToCheck);
}
