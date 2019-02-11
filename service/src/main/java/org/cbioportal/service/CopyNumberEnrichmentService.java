package org.cbioportal.service;

import org.cbioportal.model.AlterationEnrichment;
import org.cbioportal.service.exception.MolecularProfileNotFoundException;

import java.util.List;
import org.cbioportal.model.Entity;

public interface CopyNumberEnrichmentService {

    List<AlterationEnrichment> getCopyNumberEnrichments(List<Entity> set1, List<Entity> set2,
                                                        List<Integer> alterationTypes, String enrichmentType)
        throws MolecularProfileNotFoundException;
}
