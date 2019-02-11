package org.cbioportal.service.impl;

import org.cbioportal.model.AlterationEnrichment;
import org.cbioportal.model.CopyNumberCountByGene;
import org.cbioportal.model.DiscreteCopyNumberData;
import org.cbioportal.model.Entity;
import org.cbioportal.model.MolecularProfile;
import org.cbioportal.model.Sample;
import org.cbioportal.service.CopyNumberEnrichmentService;
import org.cbioportal.service.DiscreteCopyNumberService;
import org.cbioportal.service.MolecularProfileService;
import org.cbioportal.service.SampleService;
import org.cbioportal.service.exception.MolecularProfileNotFoundException;
import org.cbioportal.service.util.AlterationEnrichmentUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class CopyNumberEnrichmentServiceImpl implements CopyNumberEnrichmentService {

    @Autowired
    private DiscreteCopyNumberService discreteCopyNumberService;
    @Autowired
    private SampleService sampleService;
    @Autowired
    private MolecularProfileService molecularProfileService;
    @Autowired
    private AlterationEnrichmentUtil alterationEnrichmentUtil;

    @Override
    public List<AlterationEnrichment> getCopyNumberEnrichments(List<Entity> set1, List<Entity> set2, List<Integer> alterationTypes, String enrichmentType) throws MolecularProfileNotFoundException {
        List<Entity> allIds = new ArrayList<>(set1);
        allIds.addAll(set2);
        List<CopyNumberCountByGene> copyNumberCountByGeneListFromRepo = new ArrayList<>();
        List<DiscreteCopyNumberData> discreteCopyNumberDataList = new ArrayList<>(0);

        Map<String, List<String>> allMolecularProfileIdToEntityMap = mapMolecularProfileIdToEntityId(allIds);
        Map<String, List<String>> group1MolecularProfileIdToEntityMap = mapMolecularProfileIdToEntityId(set1);

        if (enrichmentType.equals("SAMPLE")) {
            for (String molecularProfileId : allMolecularProfileIdToEntityMap.keySet()) {
                copyNumberCountByGeneListFromRepo.addAll(discreteCopyNumberService.getSampleCountByGeneAndAlterationAndSampleIds(molecularProfileId,
                        allMolecularProfileIdToEntityMap.get(molecularProfileId), null, null));
            }
            for (String molecularProfileId : group1MolecularProfileIdToEntityMap.keySet()) {
                discreteCopyNumberDataList.addAll(discreteCopyNumberService
                    .fetchDiscreteCopyNumbersInMolecularProfile(molecularProfileId, group1MolecularProfileIdToEntityMap.get(molecularProfileId), null, alterationTypes, "ID"));
            }
        } else {
            for (String molecularProfileId : allMolecularProfileIdToEntityMap.keySet()) {
                copyNumberCountByGeneListFromRepo.addAll(discreteCopyNumberService.getPatientCountByGeneAndAlterationAndPatientIds(molecularProfileId,
                        allMolecularProfileIdToEntityMap.get(molecularProfileId), null, null));
            }
            for (String molecularProfileId : group1MolecularProfileIdToEntityMap.keySet()) {
                MolecularProfile molecularProfile = molecularProfileService.getMolecularProfile(molecularProfileId);
                List<Sample> sampleList = sampleService.getAllSamplesOfPatientsInStudy(molecularProfile.getCancerStudyIdentifier(), group1MolecularProfileIdToEntityMap.get(molecularProfileId), "ID");
                discreteCopyNumberDataList.addAll(discreteCopyNumberService
                    .fetchDiscreteCopyNumbersInMolecularProfile(molecularProfileId,
                        sampleList.stream().map(Sample::getStableId).collect(Collectors.toList()), null, alterationTypes,
                        "ID"));
            }
        }
        List<CopyNumberCountByGene> copyNumberCountByGeneList =
            new ArrayList<CopyNumberCountByGene>(copyNumberCountByGeneListFromRepo);
        copyNumberCountByGeneList.removeIf(m -> !alterationTypes.contains(m.getAlteration()));

        return alterationEnrichmentUtil.createAlterationEnrichments(set1.size(), set2.size(),
            copyNumberCountByGeneList, discreteCopyNumberDataList, enrichmentType);
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
