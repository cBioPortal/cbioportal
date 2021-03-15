package org.cbioportal.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.math3.util.Pair;
import org.cbioportal.model.AlterationCountByGene;
import org.cbioportal.model.AlterationEnrichment;
import org.cbioportal.model.EnrichmentType;
import org.cbioportal.model.MolecularProfileCaseIdentifier;
import org.cbioportal.model.MolecularProfile.MolecularAlterationType;
import org.cbioportal.model.util.Select;
import org.cbioportal.service.AlterationCountService;
import org.cbioportal.service.StructuralVariantEnrichmentService;
import org.cbioportal.service.exception.MolecularProfileNotFoundException;
import org.cbioportal.service.util.AlterationEnrichmentUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StructuralVariantEnrichmentServiceImpl implements StructuralVariantEnrichmentService {
    @Autowired
    private AlterationEnrichmentUtil<AlterationCountByGene> alterationEnrichmentUtil;
    @Autowired
    private AlterationCountService alterationCountService;

    @Override
    public List<AlterationEnrichment> getStructuralVariantEnrichments(
            Map<String, List<MolecularProfileCaseIdentifier>> molecularProfileCaseSets, EnrichmentType enrichmentType)
            throws MolecularProfileNotFoundException {

        alterationEnrichmentUtil.validateMolecularProfiles(molecularProfileCaseSets,
                Arrays.asList(MolecularAlterationType.STRUCTURAL_VARIANT), null);

        Map<String, Pair<List<AlterationCountByGene>, Long>> mutationCountsbyEntrezGeneIdAndGroup = molecularProfileCaseSets
                .entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> {
                    if (enrichmentType.equals(EnrichmentType.SAMPLE)) {
                        return alterationCountService.getSampleStructuralVariantCounts(
                                entry.getValue(),
                                Select.all(), true, true, Select.all());
                    } else {
                        return alterationCountService.getPatientStructuralVariantCounts(
                                entry.getValue(),
                                Select.all(), true, true, Select.all());
                    }
                }));

        return alterationEnrichmentUtil.createAlterationEnrichments(mutationCountsbyEntrezGeneIdAndGroup,
            molecularProfileCaseSets,
            enrichmentType);
    }

}
