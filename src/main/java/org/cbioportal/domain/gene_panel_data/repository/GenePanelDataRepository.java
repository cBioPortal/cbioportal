package org.cbioportal.domain.gene_panel_data.repository;

import java.util.Collection;
import java.util.List;
import org.cbioportal.legacy.model.GenePanelData;
import org.cbioportal.legacy.web.parameter.SampleMolecularIdentifier;

/** Gene panel assignment and profiled status of samples in molecular profiles. */
public interface GenePanelDataRepository {

  /**
   * One entry per sample of the study of each profile and per requested profile, whether or not the
   * sample was profiled. Unknown profile ids are ignored.
   */
  List<GenePanelData> getGenePanelDataByProfileIds(Collection<String> molecularProfileIds);

  /** One entry per requested (profile, sample) pair. Unknown profiles and samples are ignored. */
  List<GenePanelData> getGenePanelDataByProfileSamplePairs(
      List<SampleMolecularIdentifier> identifiers);
}
