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
        List<MutationCountByGene> mutationCountByGeneListFromRepo;
        List<Mutation> mutations;
        
        Map<String, Set<String>> molecularProfileIdToEntityIdMapForAllEntities = mapMolecularProfileIdToEntityId(allIds);
        Map<String, Set<String>> molecularProfileIdToEntityIdMapForGroup1 = mapMolecularProfileIdToEntityId(set1);
        
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
        */
        List<AlterationEnrichment> toReturn = new ArrayList<AlterationEnrichment>();
        return toReturn;
    }

    public Map<String, Set<String>> mapMolecularProfileIdToEntityId(List<Entity> entities) {
        Map<String, Set<String>> molecularProfileIdToEntityIdMap = new HashMap<>();
        for (Entity entity : entities) {
            String molecularProfileId = entity.getMolecularProfileId();
            String entityId = entity.getEntityId();
            if (!molecularProfileIdToEntityIdMap.containsKey(molecularProfileId)) {
                molecularProfileIdToEntityIdMap.put(molecularProfileId, new HashSet<>());
            }
            molecularProfileIdToEntityIdMap.get(molecularProfileId).add(entityId);
        }
        return molecularProfileIdToEntityIdMap;
    }
}




















