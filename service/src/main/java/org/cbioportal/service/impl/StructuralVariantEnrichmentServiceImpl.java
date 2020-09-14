package org.cbioportal.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.cbioportal.model.AlterationEnrichment;
import org.cbioportal.model.MolecularProfileCaseIdentifier;
import org.cbioportal.model.StructuralVariantCountByGene;
import org.cbioportal.model.MolecularProfile.MolecularAlterationType;
import org.cbioportal.model.web.parameter.EnrichmentType;
import org.cbioportal.service.StructuralVariantEnrichmentService;
import org.cbioportal.service.StructuralVariantService;
import org.cbioportal.service.exception.MolecularProfileNotFoundException;
import org.cbioportal.service.util.AlterationEnrichmentUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StructuralVariantEnrichmentServiceImpl implements StructuralVariantEnrichmentService {
    @Autowired
    private StructuralVariantService structuralVariantService;
    @Autowired
    private AlterationEnrichmentUtil<StructuralVariantCountByGene> alterationEnrichmentUtil;

    @Override
    public List<AlterationEnrichment> getStructuralVariantEnrichments(
            Map<String, List<MolecularProfileCaseIdentifier>> molecularProfileCaseSets, EnrichmentType enrichmentType)
            throws MolecularProfileNotFoundException {

        alterationEnrichmentUtil.validateMolecularProfiles(molecularProfileCaseSets,
                Arrays.asList(MolecularAlterationType.STRUCTURAL_VARIANT), null);

        Map<String, List<StructuralVariantCountByGene>> mutationCountsbyEntrezGeneIdAndGroup = molecularProfileCaseSets
                .entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> {
                    // set value of each group to list of MutationCountByGene
                    List<String> molecularProfileIds = new ArrayList<>();
                    List<String> caseIds = new ArrayList<>();

                    entry.getValue().forEach(molecularProfileCase -> {
                        molecularProfileIds.add(molecularProfileCase.getMolecularProfileId());
                        caseIds.add(molecularProfileCase.getCaseId());
                    });

                    if (enrichmentType.equals(EnrichmentType.SAMPLE)) {
                        return structuralVariantService.getSampleCountInMultipleMolecularProfiles(molecularProfileIds,
                                caseIds, null, true, true);
                    } else {
                        return structuralVariantService.getPatientCountInMultipleMolecularProfiles(molecularProfileIds,
                                caseIds, null, true, true);
                    }
                }));

        return alterationEnrichmentUtil.createAlterationEnrichments(mutationCountsbyEntrezGeneIdAndGroup,
                molecularProfileCaseSets);
    }

}
