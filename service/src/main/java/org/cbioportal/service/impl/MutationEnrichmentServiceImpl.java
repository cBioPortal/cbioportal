package org.cbioportal.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.cbioportal.model.AlterationEnrichment;
import org.cbioportal.model.MolecularProfileCaseIdentifier;
import org.cbioportal.model.MutationCountByGene;
import org.cbioportal.model.MolecularProfile.MolecularAlterationType;
import org.cbioportal.model.web.parameter.EnrichmentType;
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
    private AlterationEnrichmentUtil<MutationCountByGene> alterationEnrichmentUtil;

    @Override
    public List<AlterationEnrichment> getMutationEnrichments(
            Map<String, List<MolecularProfileCaseIdentifier>> molecularProfileCaseSets,
            EnrichmentType enrichmentType) throws MolecularProfileNotFoundException {

        alterationEnrichmentUtil.validateMolecularProfiles(molecularProfileCaseSets,
                Arrays.asList(MolecularAlterationType.MUTATION_EXTENDED, MolecularAlterationType.MUTATION_UNCALLED),
                null);

        Map<String, List<MutationCountByGene>> mutationCountsbyEntrezGeneIdAndGroup =
                molecularProfileCaseSets
                .entrySet()
                .stream()
                .collect(Collectors.toMap(
                        entry -> entry.getKey(),
                        entry -> { //set value of each group to list of MutationCountByGene
                            List<String> molecularProfileIds = new ArrayList<>();
                            List<String> sampleIds = new ArrayList<>();
    
                            entry.getValue().forEach(molecularProfileCase -> {
                                molecularProfileIds.add(molecularProfileCase.getMolecularProfileId());
                                sampleIds.add(molecularProfileCase.getCaseId());
                            });
    
                            if (enrichmentType.equals(EnrichmentType.SAMPLE)) {
                                return mutationService
                                        .getSampleCountInMultipleMolecularProfiles(molecularProfileIds,
                                                sampleIds,
                                                null,
                                                true,
                                                true);
                            } else {
                                return mutationService
                                        .getPatientCountInMultipleMolecularProfiles(molecularProfileIds,
                                                sampleIds,
                                                null,
                                                true,
                                                true);
                            }
                        }));

        return alterationEnrichmentUtil.createAlterationEnrichments(mutationCountsbyEntrezGeneIdAndGroup,
                molecularProfileCaseSets);
    }
}
