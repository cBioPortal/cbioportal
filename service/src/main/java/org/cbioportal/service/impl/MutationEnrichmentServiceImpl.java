package org.cbioportal.service.impl;

import org.cbioportal.model.AlterationEnrichment;
import org.cbioportal.model.MolecularProfile;
import org.cbioportal.model.Mutation;
import org.cbioportal.model.MutationCountByGene;
import org.cbioportal.model.Sample;
import org.cbioportal.model.MolecularProfileCase;
import org.cbioportal.service.MolecularProfileService;
import org.cbioportal.service.MutationEnrichmentService;
import org.cbioportal.service.MutationService;
import org.cbioportal.service.SampleService;
import org.cbioportal.service.exception.MolecularProfileNotFoundException;
import org.cbioportal.service.util.AlterationEnrichmentUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
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
    public List<AlterationEnrichment> getMutationEnrichments(List<MolecularProfileCase> molecularProfileCaseSet1,
            List<MolecularProfileCase> molecularProfileCaseSet2,
            String enrichmentType)
        throws MolecularProfileNotFoundException {

        List<MolecularProfileCase> allIds = new ArrayList<>(molecularProfileCaseSet1);
        allIds.addAll(molecularProfileCaseSet2);
        List<MutationCountByGene> mutationCountByGeneListFromRepo = new ArrayList<>();
        List<Mutation> mutations = new ArrayList<>();

        Map<String, List<String>> allMolecularProfileIdToCaseMap = alterationEnrichmentUtil.mapMolecularProfileIdToCaseId(allIds);
        Map<String, List<String>> group1MolecularProfileIdToCaseMap = alterationEnrichmentUtil.mapMolecularProfileIdToCaseId(molecularProfileCaseSet1);

        // get mutation count by gene list for set 1 ids
        if (enrichmentType.equals("SAMPLE")) {
            for (String molecularProfileId : allMolecularProfileIdToCaseMap.keySet()) {
                mutationCountByGeneListFromRepo.addAll(mutationService.getSampleCountByEntrezGeneIdsAndSampleIds(molecularProfileId,
                    allMolecularProfileIdToCaseMap.get(molecularProfileId), null));
            }
            for (String molecularProfileId : group1MolecularProfileIdToCaseMap.keySet()) {
                mutations.addAll(mutationService.fetchMutationsInMolecularProfile(molecularProfileId, group1MolecularProfileIdToCaseMap.get(molecularProfileId), null, null,
                    "ID", null, null, null, null));
            }
        } else {
            for (String molecularProfileId : allMolecularProfileIdToCaseMap.keySet()) {
                mutationCountByGeneListFromRepo.addAll(mutationService.getPatientCountByEntrezGeneIdsAndSampleIds(molecularProfileId,
                    allMolecularProfileIdToCaseMap.get(molecularProfileId), null));
            }
            for (String molecularProfileId : group1MolecularProfileIdToCaseMap.keySet()) {
                MolecularProfile molecularProfile = molecularProfileService.getMolecularProfile(molecularProfileId);
                List<Sample> sampleList = sampleService.getAllSamplesOfPatientsInStudy(molecularProfile.getCancerStudyIdentifier(), group1MolecularProfileIdToCaseMap.get(molecularProfileId), "ID");
                mutations.addAll(mutationService.fetchMutationsInMolecularProfile(molecularProfileId,
                    sampleList.stream().map(Sample::getStableId).collect(Collectors.toList()), null, null, "ID", null, null,
                    null, null));
            }
        }

        List<MutationCountByGene> mutationCountByGeneList = new ArrayList<MutationCountByGene>(mutationCountByGeneListFromRepo);
        return alterationEnrichmentUtil.createAlterationEnrichments(molecularProfileCaseSet1.size(), molecularProfileCaseSet2.size(),
            mutationCountByGeneList, mutations, enrichmentType);
    }
}
