package org.cbioportal.service;

import org.cbioportal.model.AlterationEnrichment;
import org.cbioportal.model.MolecularProfileCaseIdentifier;
import org.cbioportal.service.exception.MolecularProfileNotFoundException;

import java.util.List;

public interface MutationEnrichmentService {

    List<AlterationEnrichment> getMutationEnrichments(List<MolecularProfileCaseIdentifier> molecularProfileCaseSet1,
                                                      List<MolecularProfileCaseIdentifier> molecularProfileCaseSet2, String enrichmentType)
        throws MolecularProfileNotFoundException;
}
