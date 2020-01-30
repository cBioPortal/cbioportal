package org.cbioportal.service;

import java.util.List;
import java.util.Map;
import org.cbioportal.model.AlterationEnrichment;
import org.cbioportal.model.MolecularProfileCaseIdentifier;
import org.cbioportal.service.exception.MolecularProfileNotFoundException;

public interface CopyNumberEnrichmentService {
    List<AlterationEnrichment> getCopyNumberEnrichments(
        Map<String, List<MolecularProfileCaseIdentifier>> molecularProfileCaseSets,
        List<Integer> alterationTypes,
        String enrichmentType
    )
        throws MolecularProfileNotFoundException;
}
