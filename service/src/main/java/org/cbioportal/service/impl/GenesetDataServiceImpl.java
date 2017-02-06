package org.cbioportal.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.cbioportal.model.GenesetAlteration;
import org.cbioportal.model.GenesetData;
import org.cbioportal.model.GeneticProfile;
import org.cbioportal.model.Sample;
import org.cbioportal.persistence.GeneticDataRepository;
import org.cbioportal.persistence.SampleListRepository;
import org.cbioportal.service.GenesetDataService;
import org.cbioportal.service.GeneticProfileService;
import org.cbioportal.service.SampleService;
import org.cbioportal.service.exception.GeneticProfileNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GenesetDataServiceImpl implements GenesetDataService {

    @Autowired
    private GeneticDataRepository geneticDataRepository;
    @Autowired
    private SampleService sampleService;
    @Autowired
    private GeneticProfileService geneticProfileService;
    @Autowired
    private SampleListRepository sampleListRepository;

    @Override
	public List<GenesetData> fetchGenesetData(String geneticProfileId, List<String> sampleIds, List<String> genesetIds)
			throws GeneticProfileNotFoundException {

        List<GenesetData> genesetDataList = new ArrayList<>();

        String commaSeparatedSampleIdsOfGeneticProfile = geneticDataRepository
            .getCommaSeparatedSampleIdsOfGeneticProfile(geneticProfileId);
        if (commaSeparatedSampleIdsOfGeneticProfile == null) {
        	//no data, return empty list:
            return genesetDataList;
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

        List<GenesetAlteration> genesetAlterations = geneticDataRepository.getGenesetAlterations(geneticProfileId,
        		genesetIds, "SUMMARY");
        
        for (Sample sample : samples) {
            int indexOfSampleId = internalSampleIds.indexOf(sample.getInternalId());
            if (indexOfSampleId != -1) {
                for (GenesetAlteration genesetAlteration : genesetAlterations) {
                    GenesetData genesetData = new GenesetData();
                    genesetData.setGeneticProfileId(geneticProfileId);
                    genesetData.setSampleId(sample.getStableId());
                    genesetData.setGenesetId(genesetAlteration.getGenesetId());
                    genesetData.setValue(genesetAlteration.getValues().split(",")[indexOfSampleId]);
                    genesetDataList.add(genesetData);
                }
            }
        }
        
        return genesetDataList;
    }

	@Override
	public List<GenesetData> fetchGenesetData(String geneticProfileId, String sampleListId, List<String> genesetIds) throws GeneticProfileNotFoundException {
		//get list of samples for given sampleListId:
		List<String> sampleIds = sampleListRepository.getAllSampleIdsInSampleList(sampleListId);
		return fetchGenesetData(geneticProfileId, sampleIds, genesetIds);
	}

}
