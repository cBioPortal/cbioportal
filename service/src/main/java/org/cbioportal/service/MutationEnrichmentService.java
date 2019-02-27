package org.cbioportal.service;

import org.cbioportal.model.AlterationEnrichment;
import org.cbioportal.model.MolecularProfileCase;
import org.cbioportal.service.exception.MolecularProfileNotFoundException;

import java.util.List;

public interface MutationEnrichmentService {

    List<AlterationEnrichment> getMutationEnrichments(List<MolecularProfileCase> molecularProfileCaseSet1,
                                                      List<MolecularProfileCase> molecularProfileCaseSet2, String enrichmentType)
        throws MolecularProfileNotFoundException;
}
