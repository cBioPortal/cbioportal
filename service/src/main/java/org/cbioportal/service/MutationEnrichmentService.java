package org.cbioportal.service;

import org.cbioportal.model.AlterationEnrichment;
import org.cbioportal.model.Entity;
import org.cbioportal.service.exception.MolecularProfileNotFoundException;

import java.util.List;

public interface MutationEnrichmentService {

    List<AlterationEnrichment> getMutationEnrichments(List<Entity> set1,
                                                      List<Entity> set2, String enrichmentType)
        throws MolecularProfileNotFoundException;
}
