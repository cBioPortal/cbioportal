package org.cbioportal.service;

import java.util.List;
import java.util.Map;
import org.cbioportal.model.AlterationEnrichment;
import org.cbioportal.model.AlterationFilter;
import org.cbioportal.model.EnrichmentType;
import org.cbioportal.model.MolecularProfileCaseIdentifier;
import org.cbioportal.service.exception.MolecularProfileNotFoundException;

public interface AlterationEnrichmentService {

  List<AlterationEnrichment> getAlterationEnrichments(
      Map<String, List<MolecularProfileCaseIdentifier>> molecularProfileCaseSets,
      EnrichmentType enrichmentType,
      AlterationFilter alterationFilter)
      throws MolecularProfileNotFoundException;
}
