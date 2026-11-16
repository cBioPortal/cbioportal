package org.cbioportal.domain.gene_panel_data.usecase;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import org.cbioportal.domain.gene_panel_data.repository.GenePanelDataRepository;
import org.cbioportal.legacy.model.GenePanelData;
import org.cbioportal.legacy.web.parameter.GenePanelDataMultipleStudyFilter;
import org.cbioportal.legacy.web.parameter.SampleMolecularIdentifier;
import org.springframework.stereotype.Service;

/** Fetches gene panel data for molecular profiles or for (profile, sample) pairs. */
@Service
public class FetchGenePanelDataUseCase {

  private final GenePanelDataRepository genePanelDataRepository;

  public FetchGenePanelDataUseCase(GenePanelDataRepository genePanelDataRepository) {
    this.genePanelDataRepository = genePanelDataRepository;
  }

  public List<GenePanelData> execute(GenePanelDataMultipleStudyFilter filter) {
    List<String> profileIds = filter.getMolecularProfileIds();
    if (profileIds != null && !profileIds.isEmpty()) {
      return genePanelDataRepository.getGenePanelDataByProfileIds(new LinkedHashSet<>(profileIds));
    }

    List<SampleMolecularIdentifier> identifiers = filter.getSampleMolecularIdentifiers();
    if (identifiers == null || identifiers.isEmpty()) {
      return Collections.emptyList();
    }
    return genePanelDataRepository.getGenePanelDataByProfileSamplePairs(identifiers);
  }
}
