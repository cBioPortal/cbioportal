package org.cbioportal.service.impl;

import org.apache.commons.math3.util.Pair;
import org.cbioportal.model.*;
import org.cbioportal.model.util.Select;
import org.cbioportal.service.AlterationEnrichmentService;
import org.cbioportal.service.AlterationCountService;
import org.cbioportal.service.util.AlterationEnrichmentUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AlterationEnrichmentServiceImpl implements AlterationEnrichmentService {

    @Autowired
    private AlterationCountService alterationCountService;
    @Autowired
    private AlterationEnrichmentUtil<AlterationCountByGene> alterationEnrichmentUtil;

    @Override
    public List<AlterationEnrichment> getAlterationEnrichments(
            Map<String, List<MolecularProfileCaseIdentifier>> molecularProfileCaseSets,
            EnrichmentType enrichmentType,
            AlterationFilter alterationFilter) {

        Map<String, Pair<List<AlterationCountByGene>, Long>> alterationCountsbyEntrezGeneIdAndGroup = getAlterationCountsbyEntrezGeneIdAndGroup(
            molecularProfileCaseSets, enrichmentType, alterationFilter);

        return alterationEnrichmentUtil.createAlterationEnrichments(alterationCountsbyEntrezGeneIdAndGroup);
    }

    public Map<String, Pair<List<AlterationCountByGene>, Long>> getAlterationCountsbyEntrezGeneIdAndGroup(
        Map<String, List<MolecularProfileCaseIdentifier>> molecularProfileCaseSets,
        EnrichmentType enrichmentType,
        AlterationFilter alterationFilter) {
        return molecularProfileCaseSets
            .entrySet()
            .stream()
            .collect(Collectors.toMap(
                entry -> entry.getKey(), // group name
                entry -> {               // group counts

                    if (enrichmentType.equals(EnrichmentType.SAMPLE)) {
                        return alterationCountService
                            .getSampleAlterationGeneCounts(
                                entry.getValue(),
                                Select.all(),
                                true,
                                true,
                                alterationFilter);
                    } else {
                        return alterationCountService
                            .getPatientAlterationGeneCounts(
                                entry.getValue(),
                                Select.all(),
                                true,
                                true,
                                alterationFilter);
                    }
                }));
    }
}
