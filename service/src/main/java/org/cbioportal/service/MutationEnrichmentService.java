package org.cbioportal.service;

import org.cbioportal.model.AlterationEnrichment;
import org.cbioportal.service.exception.MolecularProfileNotFoundException;

import java.util.List;

public interface MutationEnrichmentService {

    List<AlterationEnrichment> getMutationEnrichments(String molecularProfileId, List<String> alteredSampleIds,
                                                      List<String> unalteredSampleIds, String enrichmentType)
        throws MolecularProfileNotFoundException;
}
