package org.cbioportal.legacy.service;

import java.util.List;
import java.util.Map;
import org.cbioportal.legacy.model.AlterationEnrichment;
import org.cbioportal.legacy.model.AlterationFilter;
import org.cbioportal.legacy.model.EnrichmentType;
import org.cbioportal.legacy.model.MolecularProfileCaseIdentifier;
import org.cbioportal.legacy.service.exception.MolecularProfileNotFoundException;

public interface AlterationEnrichmentService {

  List<AlterationEnrichment> getAlterationEnrichments(
      Map<String, List<MolecularProfileCaseIdentifier>> molecularProfileCaseSets,
      EnrichmentType enrichmentType,
      AlterationFilter alterationFilter)
      throws MolecularProfileNotFoundException;
}
