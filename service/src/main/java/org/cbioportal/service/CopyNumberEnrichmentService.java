package org.cbioportal.service;

import org.cbioportal.model.AlterationEnrichment;
import org.cbioportal.service.exception.MolecularProfileNotFoundException;

import java.util.List;
import org.cbioportal.model.MolecularProfileCaseIdentifier;

public interface CopyNumberEnrichmentService {

    List<AlterationEnrichment> getCopyNumberEnrichments(List<MolecularProfileCaseIdentifier> molecularProfileCaseSet1, List<MolecularProfileCaseIdentifier> molecularProfileCaseSet2,
                                                        List<Integer> alterationTypes, String enrichmentType)
        throws MolecularProfileNotFoundException;
}
