package org.cbioportal.persistence.mybatis;

import org.cbioportal.model.GenePanel;
import org.cbioportal.model.GenePanelData;
import org.cbioportal.model.GenePanelToGene;
import org.cbioportal.model.MolecularProfileCaseIdentifier;
import org.cbioportal.model.meta.BaseMeta;

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
}
