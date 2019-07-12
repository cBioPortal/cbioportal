package org.cbioportal.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.function.Function;

import org.cbioportal.model.GenericAssayData;
import org.cbioportal.model.GenericAssayMolecularAlteration;
import org.cbioportal.model.MolecularProfile;
import org.cbioportal.model.Sample;
import org.cbioportal.model.MolecularProfile.MolecularAlterationType;
import org.cbioportal.model.meta.GenericAssayMeta;
import org.cbioportal.persistence.GenericAssayRepository;
import org.cbioportal.persistence.MolecularDataRepository;
import org.cbioportal.persistence.SampleListRepository;
import org.cbioportal.service.GenericAssayService;
import org.cbioportal.service.MolecularProfileService;
import org.cbioportal.service.SampleService;
import org.cbioportal.service.exception.GenericAssayNotFoundException;
import org.cbioportal.service.exception.MolecularProfileNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;


@Service
public class GenericAssayServiceImpl implements GenericAssayService {

    @Autowired
    private GenericAssayRepository genericAssayRepository;

    @Autowired
    private MolecularDataRepository molecularDataRepository;

    @Autowired
    private SampleService sampleService;

    @Autowired
    private MolecularProfileService molecularProfileService;

    @Autowired
    private SampleListRepository sampleListRepository;

    @Override
    public GenericAssayMeta getGenericAssayMetaByStableId(String stableId)
        throws GenericAssayNotFoundException {

        List<GenericAssayMeta> result = genericAssayRepository.getGenericAssayMeta(Arrays.asList(stableId));
        if (result.size() == 0) {
            throw new GenericAssayNotFoundException(stableId);
        }
        else {
            return result.get(0);
        }
    }

    @Override
    public List<GenericAssayMeta> getGenericAssayMetaByStableIds(List<String> stableIds)
        throws GenericAssayNotFoundException {
        
        return genericAssayRepository.getGenericAssayMeta(stableIds);
    }

    @Override
    public List<GenericAssayData> getGenericAssayData(String molecularProfileId, String sampleListId,
                                                    List<String> genericAssayStableIds, String projection)
        throws MolecularProfileNotFoundException {
        
        validateMolecularProfile(molecularProfileId);
        List<String> sampleIds = sampleListRepository.getAllSampleIdsInSampleList(sampleListId);
        if (sampleIds.isEmpty()) {
            return Collections.emptyList();
        }
        return fetchGenericAssayData(molecularProfileId, sampleIds, genericAssayStableIds, projection);
    }

    @Override
    public List<GenericAssayData> fetchGenericAssayData(String molecularProfileId, List<String> sampleIds,
            List<String> genericAssayStableIds, String projection) throws MolecularProfileNotFoundException {

            validateMolecularProfile(molecularProfileId);
            List<GenericAssayData> molecularDataList = new ArrayList<>();
    
            String commaSeparatedSampleIdsOfMolecularProfile = molecularDataRepository
                .getCommaSeparatedSampleIdsOfMolecularProfile(molecularProfileId);
            if (commaSeparatedSampleIdsOfMolecularProfile == null) {
                return molecularDataList;
            }
            List<Integer> internalSampleIds = Arrays.stream(commaSeparatedSampleIdsOfMolecularProfile.split(","))
                .mapToInt(Integer::parseInt).boxed().collect(Collectors.toList());
            Map<Integer, Integer> internalSampleIdsMap = new HashMap<>();
            for (int lc = 0; lc < internalSampleIds.size(); lc++) {
                internalSampleIdsMap.put(internalSampleIds.get(lc), lc);
            }
    
            List<Sample> samples;
            if (sampleIds == null) {
                samples = sampleService.getSamplesByInternalIds(internalSampleIds);
            } else {
                MolecularProfile molecularProfile = molecularProfileService.getMolecularProfile(molecularProfileId);
                List<String> studyIds = new ArrayList<>();
                sampleIds.forEach(s -> studyIds.add(molecularProfile.getCancerStudyIdentifier()));
                samples = sampleService.fetchSamples(studyIds, sampleIds, "ID");
            }
    
            List<GenericAssayMolecularAlteration> molecularAlterations = molecularDataRepository.getGenericAssayMolecularAlterations(
                molecularProfileId, genericAssayStableIds, projection);
            
            for (Sample sample : samples) {
                Integer indexOfSampleId = internalSampleIdsMap.get(sample.getInternalId());
                if (indexOfSampleId != null) {
                    for (GenericAssayMolecularAlteration molecularAlteration : molecularAlterations) {
                        GenericAssayData molecularData = new GenericAssayData();
                        molecularData.setMolecularProfileId(molecularProfileId);
                        molecularData.setSampleId(sample.getStableId());
                        molecularData.setPatientId(sample.getPatientStableId());
                        molecularData.setStudyId(sample.getCancerStudyIdentifier());
                        molecularData.setGenericAssayStableId(molecularAlteration.getGenericAssayStableId());
                        molecularData.setValue(molecularAlteration.getSplitValues()[indexOfSampleId]);
                        molecularDataList.add(molecularData);
                    }
                }
            }
            
            return molecularDataList;
    }


    @Override
    public List<GenericAssayData> getGenericAssayDataInMultipleMolecularProfiles(List<String> molecularProfileIds, 
    List<String> sampleIds, List<String> genericAssayStableIds, String projection) throws MolecularProfileNotFoundException {
        List<GenericAssayData> result = new ArrayList<>();

        List<String> distinctMolecularProfileIds = molecularProfileIds.stream().distinct().sorted().collect(Collectors.toList());

        List<String> commaSeparatedSampleIdsOfMolecularProfiles = molecularDataRepository
            .getCommaSeparatedSampleIdsOfMolecularProfiles(distinctMolecularProfileIds);
    
        Map<String, Map<Integer, Integer>> internalSampleIdsMap = new HashMap<>();
        List<Integer> allInternalSampleIds = new ArrayList<>();
        
        for (int i = 0; i < distinctMolecularProfileIds.size(); i++) {
            List<Integer> internalSampleIds = Arrays.stream(commaSeparatedSampleIdsOfMolecularProfiles.get(i).split(","))
                .mapToInt(Integer::parseInt).boxed().collect(Collectors.toList());
            HashMap<Integer, Integer> molecularProfileSampleMap = new HashMap<Integer, Integer>();
            for (int lc = 0; lc < internalSampleIds.size(); lc++) {
                molecularProfileSampleMap.put(internalSampleIds.get(lc), lc);
            }
            internalSampleIdsMap.put(distinctMolecularProfileIds.get(i), molecularProfileSampleMap);
            allInternalSampleIds.addAll(internalSampleIds);
        }
    
        List<MolecularProfile> molecularProfiles = new ArrayList<>();
        List<MolecularProfile> distinctMolecularProfiles = molecularProfileService.getMolecularProfiles(
            distinctMolecularProfileIds, "SUMMARY");
        Map<String, MolecularProfile> molecularProfileMapById = distinctMolecularProfiles.stream().collect(
            Collectors.toMap(MolecularProfile::getStableId, Function.identity()));
        Map<String, List<MolecularProfile>> molecularProfileMapByStudyId = distinctMolecularProfiles.stream().collect(
            Collectors.groupingBy(MolecularProfile::getCancerStudyIdentifier));
        List<Sample> samples;
        if (sampleIds == null) {
            samples = sampleService.getSamplesByInternalIds(allInternalSampleIds);
            for (String molecularProfileId : distinctMolecularProfileIds) {
                internalSampleIdsMap.get(molecularProfileId).keySet().forEach(s -> molecularProfiles.add(molecularProfileMapById
                    .get(molecularProfileId)));
            }
        } else {
            for (String molecularProfileId : molecularProfileIds) {
                molecularProfiles.add(molecularProfileMapById.get(molecularProfileId));
            }
            List<String> studyIds = molecularProfiles.stream().map(MolecularProfile::getCancerStudyIdentifier)
                .collect(Collectors.toList());
            samples = sampleService.fetchSamples(studyIds, sampleIds, "ID");
        }
    
        List<GenericAssayMolecularAlteration> molecularAlterations = new ArrayList<>();
        for (String distinctMolecularProfileId : distinctMolecularProfileIds) {
            molecularAlterations.addAll(molecularDataRepository.getGenericAssayMolecularAlterations(
                distinctMolecularProfileId, genericAssayStableIds, projection));
        }
        Map<String, List<GenericAssayMolecularAlteration>> molecularAlterationsMap = molecularAlterations.stream().collect(
            Collectors.groupingBy(GenericAssayMolecularAlteration::getMolecularProfileId));
        
        for (Sample sample : samples) {
            for (MolecularProfile molecularProfile : molecularProfileMapByStudyId.get(sample.getCancerStudyIdentifier())) {
                String molecularProfileId = molecularProfile.getStableId();
                Integer indexOfSampleId = internalSampleIdsMap.get(molecularProfileId).get(sample.getInternalId());
                if (indexOfSampleId != null && molecularAlterationsMap.containsKey(molecularProfileId)) {
                    for (GenericAssayMolecularAlteration molecularAlteration : molecularAlterationsMap.get(molecularProfileId)) {
                        GenericAssayData molecularData = new GenericAssayData();
                        molecularData.setMolecularProfileId(molecularProfileId);
                        molecularData.setSampleId(sample.getStableId());
                        molecularData.setPatientId(sample.getPatientStableId());
                        molecularData.setStudyId(sample.getCancerStudyIdentifier());
                        molecularData.setGenericAssayStableId(molecularAlteration.getGenericAssayStableId());
                        molecularData.setValue(molecularAlteration.getSplitValues()[indexOfSampleId]);
                        result.add(molecularData);
                    }
                }
            }
        }
        return result;
    }

    private void validateMolecularProfile(String molecularProfileId) throws MolecularProfileNotFoundException {

        MolecularProfile molecularProfile = molecularProfileService.getMolecularProfile(molecularProfileId);

        if (molecularProfile.getMolecularAlterationType().equals(MolecularAlterationType.MUTATION_EXTENDED) || 
            molecularProfile.getMolecularAlterationType().equals(MolecularAlterationType.MUTATION_UNCALLED) ||
            molecularProfile.getMolecularAlterationType().equals(MolecularAlterationType.FUSION)) {

            throw new MolecularProfileNotFoundException(molecularProfileId);
        }
    }
}