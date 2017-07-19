package org.cbioportal.service.impl;

import org.cbioportal.model.AlterationEnrichment;
import org.cbioportal.model.GeneticProfile;
import org.cbioportal.model.Mutation;
import org.cbioportal.model.MutationCountByGene;
import org.cbioportal.model.Sample;
import org.cbioportal.service.GeneticProfileService;
import org.cbioportal.service.MutationEnrichmentService;
import org.cbioportal.service.MutationService;
import org.cbioportal.service.SampleService;
import org.cbioportal.service.exception.GeneticProfileNotFoundException;
import org.cbioportal.service.util.AlterationEnrichmentUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MutationEnrichmentServiceImpl implements MutationEnrichmentService {

    @Autowired
    private MutationService mutationService;
    @Autowired
    private SampleService sampleService;
    @Autowired
    private GeneticProfileService geneticProfileService;
    @Autowired
    private AlterationEnrichmentUtil alterationEnrichmentUtil;

    @Override
    @PreAuthorize("hasPermission(#geneticProfileId, 'GeneticProfile', 'read')")
    public List<AlterationEnrichment> getMutationEnrichments(String geneticProfileId, List<String> alteredIds,
                                                             List<String> unalteredIds, String enrichmentType)
        throws GeneticProfileNotFoundException {

        List<String> allIds = new ArrayList<>(alteredIds);
        allIds.addAll(unalteredIds);
        List<MutationCountByGene> mutationCountByGeneList;
        List<Mutation> mutations;
        
        if (enrichmentType.equals("SAMPLE")) {
            mutationCountByGeneList = mutationService.getSampleCountByEntrezGeneIdsAndSampleIds(geneticProfileId, 
                allIds, null);
            mutations = mutationService.fetchMutationsInGeneticProfile(geneticProfileId, alteredIds, null, null, "ID", 
                null, null, null, null);
        } else {
            mutationCountByGeneList = mutationService.getPatientCountByEntrezGeneIdsAndSampleIds(geneticProfileId,
                allIds, null);
            GeneticProfile geneticProfile = geneticProfileService.getGeneticProfile(geneticProfileId);
            List<Sample> sampleList = sampleService.getAllSamplesOfPatientsInStudy(
                geneticProfile.getCancerStudyIdentifier(), alteredIds, "ID");
            mutations = mutationService.fetchMutationsInGeneticProfile(geneticProfileId,
                sampleList.stream().map(Sample::getStableId).collect(Collectors.toList()), null, null, "ID", null, null,
                null, null);
        }

        return alterationEnrichmentUtil.createAlterationEnrichments(alteredIds.size(), unalteredIds.size(),
            mutationCountByGeneList, mutations, enrichmentType);
    }
}
