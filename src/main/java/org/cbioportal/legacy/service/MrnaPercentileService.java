package org.cbioportal.legacy.service;

import java.util.List;
import org.cbioportal.legacy.model.MrnaPercentile;
import org.cbioportal.legacy.service.exception.MolecularProfileNotFoundException;

public interface MrnaPercentileService {

  List<MrnaPercentile> fetchMrnaPercentile(
      String molecularProfileId, String sampleId, List<Integer> entrezGeneIds)
      throws MolecularProfileNotFoundException;
}
