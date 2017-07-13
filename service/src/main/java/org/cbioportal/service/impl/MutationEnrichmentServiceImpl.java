package org.cbioportal.service.impl;

import org.cbioportal.model.AlterationEnrichment;
import org.cbioportal.model.Mutation;
import org.cbioportal.model.MutationSampleCountByGene;
import org.cbioportal.service.MutationEnrichmentService;
import org.cbioportal.service.MutationService;
import org.cbioportal.service.exception.GeneticProfileNotFoundException;
import org.cbioportal.service.util.AlterationEnrichmentUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class MutationEnrichmentServiceImpl implements MutationEnrichmentService {

    @Autowired
    private MutationService mutationService;
    @Autowired
    private AlterationEnrichmentUtil alterationEnrichmentUtil;

    @Override
    @PreAuthorize("hasPermission(#geneticProfileId, 'GeneticProfile', 'read')")
    public List<AlterationEnrichment> getMutationEnrichments(String geneticProfileId, List<String> alteredSampleIds,
                                                             List<String> unalteredSampleIds)
        throws GeneticProfileNotFoundException {

        List<String> allSampleIds = new ArrayList<>(alteredSampleIds);
        allSampleIds.addAll(unalteredSampleIds);
        List<MutationSampleCountByGene> mutationSampleCountByGeneList = mutationService
            .getSampleCountByEntrezGeneIdsAndSampleIds(geneticProfileId, allSampleIds, null);

        List<Mutation> mutations = mutationService.fetchMutationsInGeneticProfile(geneticProfileId,
            alteredSampleIds, null, null, "ID", null, null, null, null);

        return alterationEnrichmentUtil.createAlterationEnrichments(alteredSampleIds.size(), unalteredSampleIds.size(),
            mutationSampleCountByGeneList, mutations);
    }
}
