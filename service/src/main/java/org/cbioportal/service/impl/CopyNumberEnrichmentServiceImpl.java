package org.cbioportal.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.cbioportal.model.AlterationCountByGene;
import org.cbioportal.model.AlterationEnrichment;
import org.cbioportal.model.MolecularProfileCaseIdentifier;
import org.cbioportal.service.CopyNumberEnrichmentService;
import org.cbioportal.service.DiscreteCopyNumberService;
import org.cbioportal.service.exception.MolecularProfileNotFoundException;
import org.cbioportal.service.util.AlterationEnrichmentUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CopyNumberEnrichmentServiceImpl implements CopyNumberEnrichmentService {

    @Autowired
    private DiscreteCopyNumberService discreteCopyNumberService;
    @Autowired
    private AlterationEnrichmentUtil alterationEnrichmentUtil;

    @Override
    public List<AlterationEnrichment> getCopyNumberEnrichments(
            Map<String, List<MolecularProfileCaseIdentifier>> molecularProfileCaseSets,
            List<Integer> alterationTypes,
            String enrichmentType) throws MolecularProfileNotFoundException {

        Map<String, List<? extends AlterationCountByGene>> copyNumberCountByGeneAndGroup =
                molecularProfileCaseSets
                .entrySet()
                .stream()
                .collect(Collectors.toMap(
                        entry -> entry.getKey(),
                        entry -> { //set value of each group to list of CopyNumberCountByGene
                            
                            List<String> molecularProfileIds = new ArrayList<>();
                            List<String> sampleIds = new ArrayList<>();
    
                            entry.getValue().forEach(molecularProfileCase -> {
                                molecularProfileIds.add(molecularProfileCase.getMolecularProfileId());
                                sampleIds.add(molecularProfileCase.getCaseId());
                            });
                            
                            if (enrichmentType.equals("SAMPLE")) {
                                return discreteCopyNumberService
                                        .getSampleCountInMultipleMolecularProfiles(molecularProfileIds,
                                                sampleIds,
                                                null,
                                                alterationTypes,
                                                false);
                            } else {
                                return discreteCopyNumberService
                                        .getPatientCountInMultipleMolecularProfiles(molecularProfileIds,
                                                sampleIds,
                                                null,
                                                alterationTypes);
                            }
                        }));

        return alterationEnrichmentUtil
                .createAlterationEnrichments(
                        copyNumberCountByGeneAndGroup,
                        molecularProfileCaseSets,
                        enrichmentType);
    }
}
