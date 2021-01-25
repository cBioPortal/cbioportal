package org.cbioportal.service.impl;

import org.cbioportal.model.*;
import org.cbioportal.model.QueryElement;
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
        Map<String, List<MolecularProfileCaseIdentifier>> molecularProfileCaseSets, final Select<MutationEventType> mutationEventTypes,
        final Select<CNA> cnaEventTypes, EnrichmentType enrichmentType) {

        Map<String, List<AlterationCountByGene>> alterationCountsbyEntrezGeneIdAndGroup = getAlterationCountsbyEntrezGeneIdAndGroup(
            molecularProfileCaseSets, mutationEventTypes, cnaEventTypes, enrichmentType);

        return alterationEnrichmentUtil.createAlterationEnrichments(alterationCountsbyEntrezGeneIdAndGroup,
                molecularProfileCaseSets);
    }

    public Map<String, List<AlterationCountByGene>> getAlterationCountsbyEntrezGeneIdAndGroup(
        Map<String, List<MolecularProfileCaseIdentifier>> molecularProfileCaseSets,
        Select<MutationEventType> mutationEventTypes,
        Select<CNA> cnaEventTypes,
        EnrichmentType enrichmentType) {
        return molecularProfileCaseSets
            .entrySet()
            .stream()
            .collect(Collectors.toMap(
                entry -> entry.getKey(), // group name
                entry -> {               // group counts

                    if (enrichmentType.equals(EnrichmentType.SAMPLE)) {
                        return alterationCountService
                            .getSampleAlterationCounts(
                                entry.getValue(),
                                Select.all(),
                                true,
                                true,
                                mutationEventTypes,
                                cnaEventTypes,
                                QueryElement.PASS);
                    } else {
                        return alterationCountService
                            .getPatientAlterationCounts(
                                entry.getValue(),
                                Select.all(),
                                true,
                                true,
                                mutationEventTypes,
                                cnaEventTypes,
                                QueryElement.PASS);
                    }
                }));
    }
}
