package org.cbioportal.service.impl;

import org.cbioportal.model.GeneGeneticAlteration;
import org.cbioportal.model.GeneGeneticData;
import org.cbioportal.model.GeneticProfile;
import org.cbioportal.model.Sample;
import org.cbioportal.persistence.GeneticDataRepository;
import org.cbioportal.service.GeneticDataService;
import org.cbioportal.service.GeneticProfileService;
import org.cbioportal.service.SampleService;
import org.cbioportal.service.exception.GeneticProfileNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GeneticDataServiceImpl implements GeneticDataService {

    @Autowired
    private GeneticDataRepository geneticDataRepository;
    @Autowired
    private SampleService sampleService;
    @Autowired
    private GeneticProfileService geneticProfileService;

    @Override
    @PreAuthorize("hasPermission(#geneticProfileId, 'GeneticProfile', 'read')")
    public List<GeneGeneticData> getGeneticData(String geneticProfileId, String sampleId, List<Integer> entrezGeneIds, 
                                            String projection)
        throws GeneticProfileNotFoundException {
        
        return fetchGeneticData(geneticProfileId, Arrays.asList(sampleId), entrezGeneIds, projection);
    }
   
    @PreAuthorize("hasPermission(#geneticProfileId, 'GeneticProfile', 'read')")
    public List<GeneGeneticData> fetchGeneticData(String geneticProfileId, List<String> sampleIds, 
                                              List<Integer> entrezGeneIds, String projection) 
        throws GeneticProfileNotFoundException {

        List<GeneGeneticData> geneticDataList = new ArrayList<>();

        String commaSeparatedSampleIdsOfGeneticProfile = geneticDataRepository
            .getCommaSeparatedSampleIdsOfGeneticProfile(geneticProfileId);
        if (commaSeparatedSampleIdsOfGeneticProfile == null) {
            return geneticDataList;
        }
        List<Integer> internalSampleIds = Arrays.stream(commaSeparatedSampleIdsOfGeneticProfile.split(","))
            .mapToInt(Integer::parseInt).boxed().collect(Collectors.toList());

        List<Sample> samples;
        if (sampleIds == null) {
            samples = sampleService.getSamplesByInternalIds(internalSampleIds);
        } else {
            GeneticProfile geneticProfile = geneticProfileService.getGeneticProfile(geneticProfileId);
            List<String> studyIds = new ArrayList<>();
            sampleIds.forEach(s -> studyIds.add(geneticProfile.getCancerStudyIdentifier()));
            samples = sampleService.fetchSamples(studyIds, sampleIds, "ID");
        }

        List<GeneGeneticAlteration> geneticAlterations = geneticDataRepository.getGeneGeneticAlterations(geneticProfileId,
            entrezGeneIds, projection);
        
        for (Sample sample : samples) {
            int indexOfSampleId = internalSampleIds.indexOf(sample.getInternalId());
            if (indexOfSampleId != -1) {
                for (GeneGeneticAlteration geneticAlteration : geneticAlterations) {
                    GeneGeneticData geneticData = new GeneGeneticData();
                    geneticData.setGeneticProfileId(geneticProfileId);
                    geneticData.setSampleId(sample.getStableId());
                    geneticData.setEntrezGeneId(geneticAlteration.getEntrezGeneId());
                    geneticData.setValue(geneticAlteration.getSplitValues()[indexOfSampleId]);
                    geneticData.setGene(geneticAlteration.getGene());
                    geneticDataList.add(geneticData);
                }
            }
        }
        
        return geneticDataList;
    }
    
    @PreAuthorize("hasPermission(#geneticProfileId, 'GeneticProfile', 'read')")
    public Integer getNumberOfSamplesInGeneticProfile(String geneticProfileId) {

        String commaSeparatedSampleIdsOfGeneticProfile = geneticDataRepository
            .getCommaSeparatedSampleIdsOfGeneticProfile(geneticProfileId);
        if (commaSeparatedSampleIdsOfGeneticProfile == null) {
            return null;
        }
        
        return commaSeparatedSampleIdsOfGeneticProfile.split(",").length;
    }
}
