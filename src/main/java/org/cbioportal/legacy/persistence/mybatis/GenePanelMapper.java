package org.cbioportal.legacy.persistence.mybatis;

import java.util.List;
import java.util.Set;
import org.cbioportal.legacy.model.GenePanel;
import org.cbioportal.legacy.model.GenePanelData;
import org.cbioportal.legacy.model.GenePanelToGene;
import org.cbioportal.legacy.model.meta.BaseMeta;

public interface GenePanelMapper {

  List<GenePanel> getAllGenePanels(
      String projection, Integer limit, Integer offset, String sortBy, String direction);

  BaseMeta getMetaGenePanels();

  GenePanel getGenePanel(String genePanelId, String projection);

  List<GenePanel> fetchGenePanels(List<String> genePanelIds, String projection);

  List<GenePanelData> getGenePanelDataBySampleListId(
      String molecularProfileId, String sampleListId);

  List<GenePanelData> getGenePanelDataBySampleIds(
      String molecularProfileId, List<String> sampleIds);

  List<GenePanelData> fetchGenePanelDataByMolecularProfileIds(Set<String> molecularProfileIds);

  List<GenePanelToGene> getGenesOfPanels(List<String> genePanelIds);
}
