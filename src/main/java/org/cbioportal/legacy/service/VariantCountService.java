package org.cbioportal.legacy.service;

import java.util.List;
import org.cbioportal.legacy.model.VariantCount;
import org.cbioportal.legacy.service.exception.MolecularProfileNotFoundException;

public interface VariantCountService {

  List<VariantCount> fetchVariantCounts(
      String molecularProfileId, List<Integer> entrezGeneIds, List<String> keywords)
      throws MolecularProfileNotFoundException;
}
