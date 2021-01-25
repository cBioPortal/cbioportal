package org.cbioportal.service;

import org.cbioportal.model.*;
import org.cbioportal.service.exception.MolecularProfileNotFoundException;

import java.util.List;
import java.util.Map;

public interface CopyNumberEnrichmentService {

    List<AlterationEnrichment> getCopyNumberEnrichments(Map<String, List<MolecularProfileCaseIdentifier>> molecularProfileCaseSets,
                                                        CNA copyNumberEventType,
                                                        EnrichmentType enrichmentType) throws MolecularProfileNotFoundException;
}
