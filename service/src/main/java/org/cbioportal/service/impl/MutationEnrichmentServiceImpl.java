package org.cbioportal.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.cbioportal.model.AlterationCountByGene;
import org.cbioportal.model.AlterationEnrichment;
import org.cbioportal.model.MolecularProfileCaseIdentifier;
import org.cbioportal.model.MutationCountByGene;
import org.cbioportal.service.MutationEnrichmentService;
import org.cbioportal.service.MutationService;
import org.cbioportal.service.exception.MolecularProfileNotFoundException;
import org.cbioportal.service.util.AlterationEnrichmentUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MutationEnrichmentServiceImpl implements MutationEnrichmentService {

    @Autowired
    private MutationService mutationService;
    @Autowired
    private AlterationEnrichmentUtil alterationEnrichmentUtil;

    @Override
    public List<AlterationEnrichment> getMutationEnrichments(
            Map<String, List<MolecularProfileCaseIdentifier>> molecularProfileCaseSets,
            String enrichmentType)
            throws MolecularProfileNotFoundException {

        Map<String, List<? extends AlterationCountByGene>> mutationCountsbyEntrezGeneIdAndGroup = new HashMap<>();

        if (enrichmentType.equals("SAMPLE")) {
            mutationCountsbyEntrezGeneIdAndGroup = molecularProfileCaseSets.entrySet().stream()
                    .collect(Collectors.toMap(
                            entry -> entry.getKey(),
                            entry -> { //set value of each group to list of MutationCountByGene
                                List<String> molecularProfileIds = new ArrayList<>();
                                List<String> sampleIds = new ArrayList<>();
        
                                entry.getValue().forEach(molecularProfileCase -> {
                                    molecularProfileIds.add(molecularProfileCase.getMolecularProfileId());
                                    sampleIds.add(molecularProfileCase.getCaseId());
                                });
                                List<MutationCountByGene> mutationCounts = mutationService
                                        .getSampleCountInMultipleMolecularProfiles(molecularProfileIds, sampleIds, null, false, false);
        
                                return mutationCounts;
                            }));
        } else {
            mutationCountsbyEntrezGeneIdAndGroup = molecularProfileCaseSets.entrySet().stream()
                    .collect(Collectors.toMap(
                            entry -> entry.getKey(),
                            entry -> { //set value of each group to list of MutationCountByGene
                                Map<String, List<MolecularProfileCaseIdentifier>> molecularProfileCaseIdentifiersMap = entry
                                        .getValue().stream()
                                        .collect(Collectors.groupingBy(MolecularProfileCaseIdentifier::getMolecularProfileId));
        
                                List<MutationCountByGene> mutationCounts = molecularProfileCaseIdentifiersMap.entrySet()
                                        .stream().flatMap(molecularProfileCaseIdentifiers -> {
                                            String molecularProfileId = molecularProfileCaseIdentifiers.getKey();
                                            List<String> caseIds = molecularProfileCaseIdentifiers.getValue().stream()
                                                    .map(MolecularProfileCaseIdentifier::getCaseId)
                                                    .collect(Collectors.toList());
                                            try {
                                                return mutationService.getPatientCountByEntrezGeneIdsAndSampleIds(
                                                        molecularProfileId, caseIds, null).stream();
                                            } catch (MolecularProfileNotFoundException e) {
                                                throw new RuntimeException(e);
                                            }
                                        }).collect(Collectors.toList());
        
                                return mutationCounts;
                            }));
        }

        return alterationEnrichmentUtil.createAlterationEnrichments(mutationCountsbyEntrezGeneIdAndGroup,
                molecularProfileCaseSets, enrichmentType);
    }
}
