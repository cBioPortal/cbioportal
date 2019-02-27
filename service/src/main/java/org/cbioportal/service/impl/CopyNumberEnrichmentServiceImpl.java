package org.cbioportal.service.impl;

import org.cbioportal.model.AlterationEnrichment;
import org.cbioportal.model.CopyNumberCountByGene;
import org.cbioportal.model.DiscreteCopyNumberData;
import org.cbioportal.model.MolecularProfileCase;
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
    public List<AlterationEnrichment> getCopyNumberEnrichments(List<MolecularProfileCase> molecularProfileCaseSet1,
            List<MolecularProfileCase> molecularProfileCaseSet2,
            List<Integer> alterationTypes,
            String enrichmentType) throws MolecularProfileNotFoundException {
        List<MolecularProfileCase> allIds = new ArrayList<>(molecularProfileCaseSet1);
        allIds.addAll(molecularProfileCaseSet2);
        List<CopyNumberCountByGene> copyNumberCountByGeneListFromRepo = new ArrayList<>();
        List<DiscreteCopyNumberData> discreteCopyNumberDataList = new ArrayList<>(0);

        Map<String, List<String>> allMolecularProfileIdToCaseMap = alterationEnrichmentUtil.mapMolecularProfileIdToCaseId(allIds);
        Map<String, List<String>> group1MolecularProfileIdToCaseMap = alterationEnrichmentUtil.mapMolecularProfileIdToCaseId(molecularProfileCaseSet1);

        if (enrichmentType.equals("SAMPLE")) {
            for (String molecularProfileId : allMolecularProfileIdToCaseMap.keySet()) {
                copyNumberCountByGeneListFromRepo.addAll(discreteCopyNumberService.getSampleCountByGeneAndAlterationAndSampleIds(molecularProfileId,
                        allMolecularProfileIdToCaseMap.get(molecularProfileId), null, null));
            }
            for (String molecularProfileId : group1MolecularProfileIdToCaseMap.keySet()) {
                discreteCopyNumberDataList.addAll(discreteCopyNumberService
                    .fetchDiscreteCopyNumbersInMolecularProfile(molecularProfileId, group1MolecularProfileIdToCaseMap.get(molecularProfileId), null, alterationTypes, "ID"));
            }
        } else {
            for (String molecularProfileId : allMolecularProfileIdToCaseMap.keySet()) {
                copyNumberCountByGeneListFromRepo.addAll(discreteCopyNumberService.getPatientCountByGeneAndAlterationAndPatientIds(molecularProfileId,
                        allMolecularProfileIdToCaseMap.get(molecularProfileId), null, null));
            }
            for (String molecularProfileId : group1MolecularProfileIdToCaseMap.keySet()) {
                MolecularProfile molecularProfile = molecularProfileService.getMolecularProfile(molecularProfileId);
                List<Sample> sampleList = sampleService.getAllSamplesOfPatientsInStudy(molecularProfile.getCancerStudyIdentifier(), group1MolecularProfileIdToCaseMap.get(molecularProfileId), "ID");
                discreteCopyNumberDataList.addAll(discreteCopyNumberService
                    .fetchDiscreteCopyNumbersInMolecularProfile(molecularProfileId,
                        sampleList.stream().map(Sample::getStableId).collect(Collectors.toList()), null, alterationTypes,
                        "ID"));
            }
        }
        List<CopyNumberCountByGene> copyNumberCountByGeneList =
            new ArrayList<CopyNumberCountByGene>(copyNumberCountByGeneListFromRepo);
        copyNumberCountByGeneList.removeIf(m -> !alterationTypes.contains(m.getAlteration()));

        return alterationEnrichmentUtil.createAlterationEnrichments(molecularProfileCaseSet1.size(), molecularProfileCaseSet2.size(),
            copyNumberCountByGeneList, discreteCopyNumberDataList, enrichmentType);
    }
}
