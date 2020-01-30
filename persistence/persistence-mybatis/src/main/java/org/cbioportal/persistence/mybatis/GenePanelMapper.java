package org.cbioportal.persistence.mybatis;

import java.util.List;
import org.cbioportal.model.GenePanel;
import org.cbioportal.model.GenePanelData;
import org.cbioportal.model.GenePanelToGene;
import org.cbioportal.model.meta.BaseMeta;

public interface GenePanelMapper {
    List<GenePanel> getAllGenePanels(
        String projection,
        Integer limit,
        Integer offset,
        String sortBy,
        String direction
    );

    BaseMeta getMetaGenePanels();

    GenePanel getGenePanel(String genePanelId, String projection);

    List<GenePanel> fetchGenePanels(
        List<String> genePanelIds,
        String projection
    );

    List<GenePanelData> getGenePanelDataBySampleListId(
        String molecularProfileId,
        String sampleListId
    );

    List<GenePanelData> getGenePanelDataBySampleIds(
        String molecularProfileId,
        List<String> sampleIds
    );

    List<GenePanelData> fetchGenePanelDataInMultipleMolecularProfiles(
        List<String> molecularProfileIds,
        List<String> sampleIds
    );

    List<GenePanelToGene> getGenesOfPanels(List<String> genePanelIds);
}
