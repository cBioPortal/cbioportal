package org.cbioportal.persistence.mybatis;

import org.cbioportal.model.GenePanel;
import org.cbioportal.model.GenePanelData;
import org.cbioportal.model.GenePanelToGene;
import org.cbioportal.model.meta.BaseMeta;

import java.util.List;

public interface GenePanelMapper {

    List<GenePanel> getAllGenePanels(String projection, Integer limit, Integer offset, String sortBy,
                                     String direction);

    BaseMeta getMetaGenePanels();

    GenePanel getGenePanel(String genePanelId, String projection);

    List<GenePanelData> getGenePanelDataBySampleListId(String geneticProfileId, String sampleListId);

    List<GenePanelData> getGenePanelDataBySampleIds(String geneticProfileId, List<String> sampleIds);

    List<GenePanelToGene> getGenesOfPanels(List<String> genePanelIds);
}
