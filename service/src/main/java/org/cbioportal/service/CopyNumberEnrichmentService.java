package org.cbioportal.service;

import org.cbioportal.model.AlterationEnrichment;
import org.cbioportal.service.exception.MolecularProfileNotFoundException;

import java.util.List;
import java.util.Map;

import org.cbioportal.model.MolecularProfileCaseIdentifier;

public interface CopyNumberEnrichmentService {

    List<AlterationEnrichment> getCopyNumberEnrichments(
            Map<String, List<MolecularProfileCaseIdentifier>> molecularProfileCaseSets, List<Integer> alterationTypes,
            String enrichmentType) throws MolecularProfileNotFoundException;
}
