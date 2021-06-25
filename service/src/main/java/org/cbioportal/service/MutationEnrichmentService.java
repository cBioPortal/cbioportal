package org.cbioportal.service;

import org.cbioportal.model.AlterationEnrichment;
import org.cbioportal.model.EnrichmentType;
import org.cbioportal.model.MolecularProfileCaseIdentifier;
import org.cbioportal.service.exception.MolecularProfileNotFoundException;

import java.util.List;
import java.util.Map;

public interface MutationEnrichmentService {

    List<AlterationEnrichment> getMutationEnrichments(
        Map<String, List<MolecularProfileCaseIdentifier>> molecularProfileCaseSets,
        EnrichmentType enrichmentType)
        throws MolecularProfileNotFoundException;
}
