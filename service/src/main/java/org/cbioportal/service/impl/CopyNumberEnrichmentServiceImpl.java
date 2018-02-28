package org.cbioportal.service.impl;

import org.cbioportal.model.AlterationEnrichment;
import org.cbioportal.model.CopyNumberCountByGene;
import org.cbioportal.model.DiscreteCopyNumberData;
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

import java.util.ArrayList;
import java.util.List;
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
    public List<AlterationEnrichment> getCopyNumberEnrichments(String molecularProfileId, List<String> alteredIds,
                                                               List<String> unalteredIds, List<Integer> alterationTypes, 
                                                               String enrichmentType)
        throws MolecularProfileNotFoundException {

        List<String> allIds = new ArrayList<>(alteredIds);
        allIds.addAll(unalteredIds);
        List<CopyNumberCountByGene> copyNumberCountByGeneListFromRepo;
        List<DiscreteCopyNumberData> discreteCopyNumberDataList;
        
        if (enrichmentType.equals("SAMPLE")) {
            copyNumberCountByGeneListFromRepo = discreteCopyNumberService.getSampleCountByGeneAndAlterationAndSampleIds(
                molecularProfileId, allIds, null, null);
            discreteCopyNumberDataList = discreteCopyNumberService
                .fetchDiscreteCopyNumbersInMolecularProfile(molecularProfileId, alteredIds, null, alterationTypes, "ID");
        } else {
            copyNumberCountByGeneListFromRepo = discreteCopyNumberService.getPatientCountByGeneAndAlterationAndPatientIds(
                molecularProfileId, allIds, null, null);
            MolecularProfile molecularProfile = molecularProfileService.getMolecularProfile(molecularProfileId);
            List<Sample> sampleList = sampleService.getAllSamplesOfPatientsInStudy(
                molecularProfile.getCancerStudyIdentifier(), alteredIds, "ID");
            discreteCopyNumberDataList = discreteCopyNumberService
                .fetchDiscreteCopyNumbersInMolecularProfile(molecularProfileId, 
                    sampleList.stream().map(Sample::getStableId).collect(Collectors.toList()), null, alterationTypes, 
                    "ID");
        }
        List<CopyNumberCountByGene> copyNumberCountByGeneList =
            new ArrayList<CopyNumberCountByGene>(copyNumberCountByGeneListFromRepo);
        copyNumberCountByGeneList.removeIf(m -> !alterationTypes.contains(m.getAlteration()));

        return alterationEnrichmentUtil.createAlterationEnrichments(alteredIds.size(), unalteredIds.size(),
            copyNumberCountByGeneList, discreteCopyNumberDataList, enrichmentType);
    }
}
