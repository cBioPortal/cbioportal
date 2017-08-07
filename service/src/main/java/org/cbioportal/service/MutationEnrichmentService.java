package org.cbioportal.service;

import org.cbioportal.model.AlterationEnrichment;
import org.cbioportal.service.exception.GeneticProfileNotFoundException;

import java.util.List;

public interface MutationEnrichmentService {

    List<AlterationEnrichment> getMutationEnrichments(String geneticProfileId, List<String> alteredSampleIds,
                                                      List<String> unalteredSampleIds, String enrichmentType)
        throws GeneticProfileNotFoundException;
}
