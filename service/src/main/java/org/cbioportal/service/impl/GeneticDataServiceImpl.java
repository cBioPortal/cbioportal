package org.cbioportal.service.impl;

import org.cbioportal.model.GeneticAlteration;
import org.cbioportal.model.GeneticData;
import org.cbioportal.model.GeneticProfile;
import org.cbioportal.model.Sample;
import org.cbioportal.persistence.GeneticDataRepository;
import org.cbioportal.service.GeneticDataService;
import org.cbioportal.service.GeneticProfileService;
import org.cbioportal.service.SampleService;
import org.cbioportal.service.exception.GeneticProfileNotFoundException;
import org.cbioportal.service.exception.SampleNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
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
    public List<GeneticData> getGeneticData(String geneticProfileId, String sampleId, List<Integer> entrezGeneIds)
        throws GeneticProfileNotFoundException, SampleNotFoundException {
        
        List<Integer> sampleIds = Arrays.stream(geneticDataRepository.getCommaSeparatedSampleIdsOfGeneticProfile(
            geneticProfileId).split(",")).mapToInt(Integer::parseInt).boxed().collect(Collectors.toList());

        GeneticProfile geneticProfile = geneticProfileService.getGeneticProfile(geneticProfileId);
        Sample sample = sampleService.getSampleInStudy(geneticProfile.getCancerStudyIdentifier(), sampleId);
        int indexOfSampleId = sampleIds.indexOf(sample.getInternalId());

        List<GeneticAlteration> geneticAlterations = geneticDataRepository.getGeneticAlterations(geneticProfileId,
            entrezGeneIds);

        List<GeneticData> geneticDataList = new ArrayList<>();
        for (GeneticAlteration geneticAlteration : geneticAlterations) {
            GeneticData geneticData = new GeneticData();
            geneticData.setGeneticProfileId(geneticProfileId);
            geneticData.setSampleId(sampleId);
            geneticData.setEntrezGeneId(geneticAlteration.getEntrezGeneId());
            geneticData.setValue(geneticAlteration.getValues().split(",")[indexOfSampleId]);
            geneticDataList.add(geneticData);
        }

        return geneticDataList;
    }

    @Override
    public List<GeneticData> getGeneticDataOfAllSamplesOfGeneticProfile(String geneticProfileId, 
                                                                        List<Integer> entrezGeneIds) 
        throws GeneticProfileNotFoundException {

        List<Integer> sampleIds = Arrays.stream(geneticDataRepository.getCommaSeparatedSampleIdsOfGeneticProfile(
            geneticProfileId).split(",")).mapToInt(Integer::parseInt).boxed().collect(Collectors.toList());

        List<GeneticAlteration> geneticAlterations = geneticDataRepository.getGeneticAlterations(geneticProfileId,
            entrezGeneIds);

        List<Sample> samples = sampleService.getSamplesByInternalIds(sampleIds);
        List<GeneticData> geneticDataList = new ArrayList<>();

        for (GeneticAlteration geneticAlteration : geneticAlterations) {
            String[] splitValues = geneticAlteration.getValues().split(",");
            for (int i = 0; i < sampleIds.size(); i++) {
                Integer sampleId = sampleIds.get(i);
                GeneticData geneticData = new GeneticData();
                geneticData.setGeneticProfileId(geneticProfileId);
                Sample sample = samples.stream().filter(s -> s.getInternalId().equals(sampleId)).findFirst().get();
                geneticData.setSampleId(sample.getStableId());
                geneticData.setEntrezGeneId(geneticAlteration.getEntrezGeneId());
                geneticData.setValue(splitValues[i]);
                geneticDataList.add(geneticData);
            }
        }
        
        return geneticDataList;
    }
}
