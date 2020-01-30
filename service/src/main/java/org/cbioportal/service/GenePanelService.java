package org.cbioportal.service;

import java.util.List;
import org.cbioportal.model.GenePanel;
import org.cbioportal.model.GenePanelData;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.service.exception.GenePanelNotFoundException;
import org.cbioportal.service.exception.MolecularProfileNotFoundException;

public interface GenePanelService {
    List<GenePanel> getAllGenePanels(
        String projection,
        Integer pageSize,
        Integer pageNumber,
        String sortBy,
        String direction
    );

    BaseMeta getMetaGenePanels();

    GenePanel getGenePanel(String genePanelId)
        throws GenePanelNotFoundException;

    List<GenePanelData> getGenePanelData(
        String molecularProfileId,
        String sampleListId
    )
        throws MolecularProfileNotFoundException;

    List<GenePanelData> fetchGenePanelData(
        String molecularProfileId,
        List<String> sampleIds
    )
        throws MolecularProfileNotFoundException;

    List<GenePanelData> fetchGenePanelDataInMultipleMolecularProfiles(
        List<String> molecularProfileIds,
        List<String> sampleIds
    );

    List<GenePanel> fetchGenePanels(
        List<String> genePanelIds,
        String projection
    );
}
