package org.cbioportal.service.impl;

import org.cbioportal.model.GenePanel;
import org.cbioportal.model.GenePanelData;
import org.cbioportal.model.GenePanelToGene;
import org.cbioportal.model.MolecularProfile;
import org.cbioportal.model.Sample;
import org.cbioportal.model.SampleList;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.persistence.GenePanelRepository;
import org.cbioportal.persistence.SampleListRepository;
import org.cbioportal.service.GenePanelService;
import org.cbioportal.service.MolecularProfileService;
import org.cbioportal.service.SampleListService;
import org.cbioportal.service.SampleService;
import org.cbioportal.service.exception.GenePanelNotFoundException;
import org.cbioportal.service.exception.MolecularProfileNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.apache.commons.collections.map.MultiKeyMap;
import org.apache.commons.math3.util.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class GenePanelServiceImpl implements GenePanelService {

    private static final String SEQUENCED_LIST_SUFFIX = "_sequenced";
    
    @Autowired
    private GenePanelRepository genePanelRepository;
    @Autowired
    private MolecularProfileService molecularProfileService;
    @Autowired
    private SampleListRepository sampleListRepository;
    @Autowired
    private SampleListService sampleListService;
    @Autowired
    private SampleService sampleService;

    @Override
    public List<GenePanel> getAllGenePanels(String projection, Integer pageSize, Integer pageNumber, String sortBy, 
                                            String direction) {
        
        List<GenePanel> genePanels = genePanelRepository.getAllGenePanels(projection, pageSize, pageNumber, sortBy, 
            direction);

        if (projection.equals("DETAILED")) {

            List<GenePanelToGene> genePanelToGeneList = genePanelRepository.getGenesOfPanels(genePanels
                .stream().map(GenePanel::getStableId).collect(Collectors.toList()));

            genePanels.forEach(g -> g.setGenes(genePanelToGeneList.stream().filter(p -> p.getGenePanelId()
                .equals(g.getStableId())).collect(Collectors.toList())));
        }
        
        return genePanels;
    }

    @Override
    public BaseMeta getMetaGenePanels() {
        
        return genePanelRepository.getMetaGenePanels();
    }

    @Override
    public GenePanel getGenePanel(String genePanelId) throws GenePanelNotFoundException {

        GenePanel genePanel = genePanelRepository.getGenePanel(genePanelId);
        if (genePanel == null) {
            throw new GenePanelNotFoundException(genePanelId);
        }
        
        genePanel.setGenes(genePanelRepository.getGenesOfPanels(Arrays.asList(genePanelId)));
        return genePanel;
    }

    @Override
	public List<GenePanel> fetchGenePanels(List<String> genePanelIds, String projection) {

        List<GenePanel> genePanels = genePanelRepository.fetchGenePanels(genePanelIds, projection);
        
        if (projection.equals("DETAILED")) {

            List<GenePanelToGene> genePanelToGeneList = genePanelRepository.getGenesOfPanels(genePanels
                .stream().map(GenePanel::getStableId).collect(Collectors.toList()));

            genePanels.forEach(g -> g.setGenes(genePanelToGeneList.stream().filter(p -> p.getGenePanelId()
                .equals(g.getStableId())).collect(Collectors.toList())));
        }

        return genePanels;
	}
    
    @Override
    public List<GenePanelData> getGenePanelData(String molecularProfileId, String sampleListId) 
        throws MolecularProfileNotFoundException {

        molecularProfileService.getMolecularProfile(molecularProfileId);
        List<String> sampleIds = sampleListRepository.getAllSampleIdsInSampleList(sampleListId);
        if (sampleIds.isEmpty()) {
            return Collections.<GenePanelData>emptyList();
        }
        List<String> molecularProfileIds = new ArrayList<>();
        sampleIds.forEach(s -> molecularProfileIds.add(molecularProfileId));

        List<GenePanelData> genePanelData = genePanelRepository.getGenePanelData(molecularProfileId, sampleListId);
        return createGenePanelData(createGenePanelDataMap(genePanelData), molecularProfileIds, sampleIds);
    }

    @Override
    public List<GenePanelData> fetchGenePanelData(String molecularProfileId, List<String> sampleIds) 
        throws MolecularProfileNotFoundException {

        molecularProfileService.getMolecularProfile(molecularProfileId);
        List<String> molecularProfileIds = new ArrayList<>();
        sampleIds.forEach(s -> molecularProfileIds.add(molecularProfileId));

        List<GenePanelData> genePanelData = genePanelRepository.fetchGenePanelData(molecularProfileId, sampleIds);
        return createGenePanelData(createGenePanelDataMap(genePanelData), molecularProfileIds, sampleIds);
    }

    @Override
    public List<GenePanelData> fetchGenePanelDataInMultipleMolecularProfiles(List<String> molecularProfileIds,
            List<String> sampleIds) {

        // TODO: remove this block once data is fixed
        boolean hasFusionProfileIdsInQuery = false;
        for (int i = 0; i < molecularProfileIds.size(); i++) {
            if (molecularProfileIds.get(i).endsWith("_fusion")) {
                hasFusionProfileIdsInQuery = true;
                break;
            }
        }
        List<String> updatedMolecularProfileIds = new ArrayList<>(molecularProfileIds);
        List<String> updatedSampleIds = new ArrayList<>(sampleIds);

        if (hasFusionProfileIdsInQuery) {
            MultiKeyMap queriedIdsMap = new MultiKeyMap();
            for (int i = 0; i < molecularProfileIds.size(); i++) {
                queriedIdsMap.put(molecularProfileIds.get(i), sampleIds.get(i), true);
            }

            for (int i = 0; i < molecularProfileIds.size(); i++) {
                if (molecularProfileIds.get(i).endsWith("_fusion")) {
                    String mutationProfileId = molecularProfileIds.get(i).replace("_fusion", "_mutations");
                    if (!queriedIdsMap.containsKey(mutationProfileId, sampleIds.get(i))) {
                        updatedMolecularProfileIds.add(mutationProfileId);
                        updatedSampleIds.add(sampleIds.get(i));
                    }
                }
            }
        }
        // TODO: remove this block once data is fixed

        List<GenePanelData> genePanelData = genePanelRepository
                .fetchGenePanelDataInMultipleMolecularProfiles(updatedMolecularProfileIds, updatedSampleIds);
        return createGenePanelData(createGenePanelDataMap(genePanelData), molecularProfileIds, sampleIds);
    }

    @Override
    public List<GenePanelData> fetchGenePanelDataInMultipleMolecularProfilesByPatientIds(
            List<String> molecularProfileIds, List<String> patientIds) {

        List<GenePanelData> genePanelData = genePanelRepository
                .fetchGenePanelDataInMultipleMolecularProfilesByPatientIds(molecularProfileIds, patientIds);
        return createGenePanelDatabyPatientIds(createGenePanelDataMap(genePanelData), molecularProfileIds, patientIds);
    }

    private List<GenePanelData> createGenePanelData(MultiKeyMap genePanelDataMap, List<String> molecularProfileIds,
        List<String> sampleIds) {

        Map<String, MolecularProfile> molecularProfileMap = molecularProfileService
                .getMolecularProfiles(new ArrayList<>(new HashSet<>(molecularProfileIds)), "SUMMARY")
                .stream()
                .collect(Collectors.toMap(MolecularProfile::getStableId, Function.identity()));

        List<String> filteredStudyIds = new ArrayList<String>();
        List<String> filteredSampleIds = new ArrayList<String>();

        for (int index = 0; index < molecularProfileIds.size(); index++) {
            String molecularProfileId = molecularProfileIds.get(index);
            if (molecularProfileMap.containsKey(molecularProfileId)) {
                filteredStudyIds.add(molecularProfileMap.get(molecularProfileId).getCancerStudyIdentifier());
                filteredSampleIds.add(sampleIds.get(index));
            }
        }

        MultiKeyMap samples = createSampleMap(filteredStudyIds, filteredSampleIds);

        List<String> sequencedSampleListIds = filteredStudyIds
                .stream()
                .distinct()
                .map(studyId -> studyId + SEQUENCED_LIST_SUFFIX)
                .collect(Collectors.toList());
        
        
        Map<String, List<SampleList>> sampleListMap = sampleListService.fetchSampleLists(sequencedSampleListIds, "DETAILED")
            .stream().collect(Collectors.groupingBy(SampleList::getCancerStudyIdentifier));

        List<GenePanelData> resultGenePanelDataList = new ArrayList<>();

        for(int i = 0; i < filteredSampleIds.size(); i++) {
            String sampleId = filteredSampleIds.get(i);
            String molecularProfileId = molecularProfileIds.get(i);
            MolecularProfile molecularProfile = molecularProfileMap.get(molecularProfileId);
            String studyId = molecularProfile.getCancerStudyIdentifier();
            
            if (samples.containsKey(sampleId, studyId)) {
                GenePanelData resultGenePanelData = new GenePanelData();
                resultGenePanelData.setStudyId(studyId);
                Optional<GenePanelData> genePanelData =
                    Optional.ofNullable((GenePanelData)genePanelDataMap.get(molecularProfileId, sampleId));
                if (genePanelData.isPresent()) {
                    resultGenePanelData.setGenePanelId(genePanelData.get().getGenePanelId());
                    resultGenePanelData.setProfiled(true);
                } else {
                    List<SampleList> sampleLists = sampleListMap.get(studyId);
                    resultGenePanelData.setProfiled(
                            molecularProfile.getMolecularAlterationType().equals(MolecularProfile.MolecularAlterationType.MUTATION_EXTENDED) && 
                            (sampleLists == null || sampleLists.get(0).getSampleIds().contains(sampleId)));
                }
                resultGenePanelData.setMolecularProfileId(molecularProfileId);
                resultGenePanelData.setSampleId(sampleId);
                
                Sample sample = (Sample)samples.get(sampleId, studyId);
                resultGenePanelData.setPatientId(sample.getPatientStableId());
                resultGenePanelDataList.add(resultGenePanelData);
            }
        }

        return resultGenePanelDataList;
    }

    private List<GenePanelData> createGenePanelDatabyPatientIds(
            MultiKeyMap genePanelDataMap,
            List<String> molecularProfileIds,
            List<String> patientIds) {

        Map<String, MolecularProfile> molecularProfileMap = molecularProfileService
                .getMolecularProfiles(new ArrayList<>(new HashSet<>(molecularProfileIds)), "SUMMARY")
                .stream()
                .collect(Collectors.toMap(MolecularProfile::getStableId, Function.identity()));

        List<String> filteredStudyIds = new ArrayList<String>();
        List<String> filteredPatientIds = new ArrayList<String>();

        for (int index = 0; index < molecularProfileIds.size(); index++) {
            String molecularProfileId = molecularProfileIds.get(index);
            if (molecularProfileMap.containsKey(molecularProfileId)) {
                filteredStudyIds.add(molecularProfileMap.get(molecularProfileId).getCancerStudyIdentifier());
                filteredPatientIds.add(patientIds.get(index));
            }
        }

        Map<Pair<String, String>, List<Sample>> sampleSet = createSampleMapByPatientId(filteredStudyIds, filteredPatientIds);

        List<String> sequencedSampleListIds = filteredStudyIds
                .stream()
                .distinct()
                .map(studyId -> studyId + SEQUENCED_LIST_SUFFIX)
                .collect(Collectors.toList());

        Map<String, List<SampleList>> sampleListMap = sampleListService
                .fetchSampleLists(sequencedSampleListIds, "DETAILED")
                .stream()
                .collect(Collectors.groupingBy(SampleList::getCancerStudyIdentifier));

        List<GenePanelData> resultGenePanelDataList = new ArrayList<>();

        for(int i = 0; i < filteredPatientIds.size(); i++) {
            String patinetId = filteredPatientIds.get(i);
            String molecularProfileId = molecularProfileIds.get(i);
            MolecularProfile molecularProfile = molecularProfileMap.get(molecularProfileId);
            String studyId = molecularProfile.getCancerStudyIdentifier();
            Pair<String, String> uniqueKey = new Pair<>(studyId, patinetId);

            if (sampleSet.containsKey(uniqueKey)) {
                sampleSet.get(uniqueKey).forEach(sample -> {
                    String sampleId = sample.getStableId();
                    GenePanelData resultGenePanelData = new GenePanelData();
                    resultGenePanelData.setStudyId(studyId);
                    Optional<GenePanelData> genePanelData =
                        Optional.ofNullable((GenePanelData)genePanelDataMap.get(molecularProfileId, sampleId));
                    if (genePanelData.isPresent()) {
                        resultGenePanelData.setGenePanelId(genePanelData.get().getGenePanelId());
                        resultGenePanelData.setProfiled(true);
                    } else {
                        List<SampleList> sampleLists = sampleListMap.get(studyId);
                        if (molecularProfile.getMolecularAlterationType().equals(MolecularProfile.MolecularAlterationType.MUTATION_EXTENDED) && 
                            (sampleLists == null || (sampleLists != null && sampleLists.get(0).getSampleIds().contains(sampleId)))) {
                            resultGenePanelData.setProfiled(true);
                        } else {
                            resultGenePanelData.setProfiled(false);
                        }
                    }
                    resultGenePanelData.setMolecularProfileId(molecularProfileId);
                    resultGenePanelData.setSampleId(sampleId);
                    resultGenePanelData.setPatientId(sample.getPatientStableId());
                    resultGenePanelDataList.add(resultGenePanelData);
                });
            }
        }

        return resultGenePanelDataList;
    }

    private MultiKeyMap createGenePanelDataMap(List<GenePanelData> genePanelDataList)
    {
        MultiKeyMap toReturn = new MultiKeyMap();
        for (GenePanelData gpd : genePanelDataList) {
            toReturn.put(gpd.getMolecularProfileId(), gpd.getSampleId(), gpd);
        }
        return toReturn;
    }

    private MultiKeyMap createSampleMap(List<String> studyIds, List<String> sampleIds)
    {
        MultiKeyMap toReturn = new MultiKeyMap();
        for (Sample sample : sampleService.fetchSamples(studyIds, sampleIds, "ID")) {
            toReturn.put(sample.getStableId(), sample.getCancerStudyIdentifier(), sample);
        }
        return toReturn;
    }

    private Map<Pair<String, String>, List<Sample>> createSampleMapByPatientId(List<String> studyIds, List<String> patientIds)
    {
        return sampleService
                .getSamplesOfPatientsInMultipleStudies(studyIds, patientIds, "SUMMARY")
                .stream()
                .collect(Collectors.groupingBy(sample ->new Pair<>(sample.getCancerStudyIdentifier(), sample.getPatientStableId())));
    }
}
