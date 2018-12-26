package org.cbioportal.service.impl;

import org.cbioportal.model.AlterationEnrichment;
import org.cbioportal.model.MolecularProfile;
import org.cbioportal.model.Mutation;
import org.cbioportal.model.MutationCountByGene;
import org.cbioportal.model.Sample;
import org.cbioportal.service.MolecularProfileService;
import org.cbioportal.service.MutationEnrichmentService;
import org.cbioportal.service.MutationService;
import org.cbioportal.service.SampleService;
import org.cbioportal.service.exception.MolecularProfileNotFoundException;
import org.cbioportal.service.util.AlterationEnrichmentUtil;
import org.springframework.beans.factory.annotation.Autowired;
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
    private MolecularProfileService molecularProfileService;
    @Autowired
    private AlterationEnrichmentUtil alterationEnrichmentUtil;

    @Override
    public List<AlterationEnrichment> getMutationEnrichments(String molecularProfileId, List<String> alteredIds,
                                                             List<String> unalteredIds, String enrichmentType)
        throws MolecularProfileNotFoundException {

        List<String> allIds = new ArrayList<>(alteredIds);
        allIds.addAll(unalteredIds);
        List<MutationCountByGene> mutationCountByGeneListFromRepo;
        List<Mutation> mutations;
        
        if (enrichmentType.equals("SAMPLE")) {
            mutationCountByGeneListFromRepo = mutationService.getSampleCountByEntrezGeneIdsAndSampleIds(molecularProfileId, 
                allIds, null);
            mutations = mutationService.fetchMutationsInMolecularProfile(molecularProfileId, alteredIds, null, null, 
                "ID", null, null, null, null);
        } else {
            mutationCountByGeneListFromRepo = mutationService.getPatientCountByEntrezGeneIdsAndSampleIds(molecularProfileId,
                allIds, null);
            MolecularProfile molecularProfile = molecularProfileService.getMolecularProfile(molecularProfileId);
            List<Sample> sampleList = sampleService.getAllSamplesOfPatientsInStudy(
                molecularProfile.getCancerStudyIdentifier(), alteredIds, "ID");
            mutations = mutationService.fetchMutationsInMolecularProfile(molecularProfileId,
                sampleList.stream().map(Sample::getStableId).collect(Collectors.toList()), null, null, "ID", null, null,
                null, null);
        }

        List<MutationCountByGene> mutationCountByGeneList = new ArrayList<MutationCountByGene>(mutationCountByGeneListFromRepo);
        return alterationEnrichmentUtil.createAlterationEnrichments(alteredIds.size(), unalteredIds.size(),
            mutationCountByGeneList, mutations, enrichmentType);
    }
}
