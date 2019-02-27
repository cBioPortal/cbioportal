package org.cbioportal.service;

import org.cbioportal.model.AlterationEnrichment;
import org.cbioportal.service.exception.MolecularProfileNotFoundException;

import java.util.List;
import org.cbioportal.model.MolecularProfileCase;

public interface CopyNumberEnrichmentService {

    List<AlterationEnrichment> getCopyNumberEnrichments(List<MolecularProfileCase> molecularProfileCaseSet1, List<MolecularProfileCase> molecularProfileCaseSet2,
                                                        List<Integer> alterationTypes, String enrichmentType)
        throws MolecularProfileNotFoundException;
}
