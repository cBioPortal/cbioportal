package org.cbioportal.service.impl;

import org.cbioportal.model.AlterationEnrichment;
import org.cbioportal.model.CNA;
import org.cbioportal.model.CopyNumberCountByGene;
import org.cbioportal.model.EnrichmentType;
import org.cbioportal.model.MolecularProfileCaseIdentifier;
import org.cbioportal.model.util.Select;
import org.cbioportal.service.AlterationCountService;
import org.cbioportal.service.CopyNumberEnrichmentService;
import org.cbioportal.service.exception.MolecularProfileNotFoundException;
import org.cbioportal.service.util.AlterationEnrichmentUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CopyNumberEnrichmentServiceImpl implements CopyNumberEnrichmentService {

    @Autowired
    private AlterationCountService alterationCountService;
    @Autowired
    private AlterationEnrichmentUtil<CopyNumberCountByGene> alterationEnrichmentUtil;

    @Override
    public List<AlterationEnrichment> getCopyNumberEnrichments(
        Map<String, List<MolecularProfileCaseIdentifier>> molecularProfileCaseSets,
        CNA copyNumberEventType,
        EnrichmentType enrichmentType) throws MolecularProfileNotFoundException {

        Map<String, List<CopyNumberCountByGene>> copyNumberCountByGeneAndGroup = getCopyNumberCountByGeneAndGroup(
            molecularProfileCaseSets,
            copyNumberEventType,
            enrichmentType);

        return alterationEnrichmentUtil
            .createAlterationEnrichments(
                copyNumberCountByGeneAndGroup,
                molecularProfileCaseSets);
    }

    public Map<String, List<CopyNumberCountByGene>> getCopyNumberCountByGeneAndGroup(
        Map<String, List<MolecularProfileCaseIdentifier>> molecularProfileCaseSets,
        CNA copyNumberEventType,
        EnrichmentType enrichmentType) {
        return molecularProfileCaseSets
            .entrySet()
            .stream()
            .collect(Collectors.toMap(
                entry -> entry.getKey(),
                entry -> { //set value of each group to list of CopyNumberCountByGene

                    List<String> molecularProfileIds = new ArrayList<>();
                    List<String> sampleIds = new ArrayList<>();

                    Select<CNA> cnaTypes = Select.byValues(Arrays.asList(copyNumberEventType));

                    if (enrichmentType.name().equals("SAMPLE")) {
                        return alterationCountService.getSampleCnaCounts(
                            entry.getValue(),
                            Select.all(),
                            true,
                            true,
                            cnaTypes);
                    } else {
                        return alterationCountService.getPatientCnaCounts(
                            entry.getValue(),
                            Select.all(),
                            true,
                            true,
                            cnaTypes);
                    }
                }));
    }
    
}
