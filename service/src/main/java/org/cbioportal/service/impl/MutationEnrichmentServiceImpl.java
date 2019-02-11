package org.cbioportal.service.impl;

import org.cbioportal.model.AlterationEnrichment;
import org.cbioportal.model.MolecularProfile;
import org.cbioportal.model.Mutation;
import org.cbioportal.model.MutationCountByGene;
import org.cbioportal.model.Sample;
import org.cbioportal.model.Entity;
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
    public List<AlterationEnrichment> getMutationEnrichments(List<Entity> set1, List<Entity> set2, String enrichmentType)
        throws MolecularProfileNotFoundException {

        List<Entity> allIds = new ArrayList<>(set1);
        allIds.addAll(set2);
        List<MutationCountByGene> mutationCountByGeneListFromRepo = new ArrayList<>();
        List<Mutation> mutations = new ArrayList<>();

        Map<String, List<String>> allMolecularProfileIdToEntityMap = mapMolecularProfileIdToEntityId(allIds);
        Map<String, List<String>> group1MolecularProfileIdToEntityMap = mapMolecularProfileIdToEntityId(set1);

        // get mutation count by gene list for set 1 ids
        if (enrichmentType.equals("SAMPLE")) {
            for (String molecularProfileId : allMolecularProfileIdToEntityMap.keySet()) {
                mutationCountByGeneListFromRepo.addAll(mutationService.getSampleCountByEntrezGeneIdsAndSampleIds(molecularProfileId,
                    allMolecularProfileIdToEntityMap.get(molecularProfileId), null));
            }
            for (String molecularProfileId : group1MolecularProfileIdToEntityMap.keySet()) {
                mutations.addAll(mutationService.fetchMutationsInMolecularProfile(molecularProfileId, group1MolecularProfileIdToEntityMap.get(molecularProfileId), null, null,
                    "ID", null, null, null, null));
            }
        } else {
            for (String molecularProfileId : allMolecularProfileIdToEntityMap.keySet()) {
                mutationCountByGeneListFromRepo.addAll(mutationService.getPatientCountByEntrezGeneIdsAndSampleIds(molecularProfileId,
                    allMolecularProfileIdToEntityMap.get(molecularProfileId), null));
            }
            for (String molecularProfileId : group1MolecularProfileIdToEntityMap.keySet()) {
                MolecularProfile molecularProfile = molecularProfileService.getMolecularProfile(molecularProfileId);
                List<Sample> sampleList = sampleService.getAllSamplesOfPatientsInStudy(molecularProfile.getCancerStudyIdentifier(), group1MolecularProfileIdToEntityMap.get(molecularProfileId), "ID");
                mutations.addAll(mutationService.fetchMutationsInMolecularProfile(molecularProfileId,
                    sampleList.stream().map(Sample::getStableId).collect(Collectors.toList()), null, null, "ID", null, null,
                    null, null));
            }
        }

        List<MutationCountByGene> mutationCountByGeneList = new ArrayList<MutationCountByGene>(mutationCountByGeneListFromRepo);
        return alterationEnrichmentUtil.createAlterationEnrichments(set1.size(), set2.size(),
            mutationCountByGeneList, mutations, enrichmentType);
    }

    private Map<String, List<String>> mapMolecularProfileIdToEntityId(List<Entity> entities) {
        Map<String, List<String>> molecularProfileIdToEntityIdMap = new HashMap<>();
        for (Entity entity : entities) {
            String molecularProfileId = entity.getMolecularProfileId();
            String entityId = entity.getEntityId();
            if (!molecularProfileIdToEntityIdMap.containsKey(molecularProfileId)) {
                molecularProfileIdToEntityIdMap.put(molecularProfileId, new ArrayList<>());
            }
            molecularProfileIdToEntityIdMap.get(molecularProfileId).add(entityId);
        }
        return molecularProfileIdToEntityIdMap;
    }
}




















