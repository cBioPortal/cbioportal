package org.cbioportal.service.impl;

import org.cbioportal.model.AlterationEnrichment;
import org.cbioportal.model.CopyNumberCountByGene;
import org.cbioportal.model.DiscreteCopyNumberData;
import org.cbioportal.model.GeneticProfile;
import org.cbioportal.model.Sample;
import org.cbioportal.service.CopyNumberEnrichmentService;
import org.cbioportal.service.DiscreteCopyNumberService;
import org.cbioportal.service.GeneticProfileService;
import org.cbioportal.service.SampleService;
import org.cbioportal.service.exception.GeneticProfileNotFoundException;
import org.cbioportal.service.util.AlterationEnrichmentUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
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
    private GeneticProfileService geneticProfileService;
    @Autowired
    private AlterationEnrichmentUtil alterationEnrichmentUtil;
    
    @Override
    @PreAuthorize("hasPermission(#geneticProfileId, 'GeneticProfile', 'read')")
    public List<AlterationEnrichment> getCopyNumberEnrichments(String geneticProfileId, List<String> alteredIds,
                                                               List<String> unalteredIds, List<Integer> alterationTypes, 
                                                               String enrichmentType)
        throws GeneticProfileNotFoundException {

        List<String> allIds = new ArrayList<>(alteredIds);
        allIds.addAll(unalteredIds);
        List<CopyNumberCountByGene> copyNumberCountByGeneList;
        List<DiscreteCopyNumberData> discreteCopyNumberDataList;
        
        if (enrichmentType.equals("SAMPLE")) {
            copyNumberCountByGeneList = discreteCopyNumberService.getSampleCountByGeneAndAlterationAndSampleIds(
                geneticProfileId, allIds, null, null);
            discreteCopyNumberDataList = discreteCopyNumberService
                .fetchDiscreteCopyNumbersInGeneticProfile(geneticProfileId, alteredIds, null, alterationTypes, "ID");
        } else {
            copyNumberCountByGeneList = discreteCopyNumberService.getPatientCountByGeneAndAlterationAndPatientIds(
                geneticProfileId, allIds, null, null);
            GeneticProfile geneticProfile = geneticProfileService.getGeneticProfile(geneticProfileId);
            List<Sample> sampleList = sampleService.getAllSamplesOfPatientsInStudy(
                geneticProfile.getCancerStudyIdentifier(), alteredIds, "ID");
            discreteCopyNumberDataList = discreteCopyNumberService
                .fetchDiscreteCopyNumbersInGeneticProfile(geneticProfileId, 
                    sampleList.stream().map(Sample::getStableId).collect(Collectors.toList()), null, alterationTypes, 
                    "ID");
        }
        copyNumberCountByGeneList.removeIf(m -> !alterationTypes.contains(m.getAlteration()));

        return alterationEnrichmentUtil.createAlterationEnrichments(alteredIds.size(), unalteredIds.size(),
            copyNumberCountByGeneList, discreteCopyNumberDataList, enrichmentType);
    }
}
