package org.cbioportal.service.impl;

import org.cbioportal.model.GeneMolecularAlteration;
import org.cbioportal.model.GeneMolecularData;
import org.cbioportal.model.MolecularProfile;
import org.cbioportal.model.MolecularProfile.MolecularAlterationType;
import org.cbioportal.model.MolecularProfileSamples;
import org.cbioportal.model.Sample;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.persistence.MolecularDataRepository;
import org.cbioportal.persistence.SampleListRepository;
import org.cbioportal.service.MolecularDataService;
import org.cbioportal.service.MolecularProfileService;
import org.cbioportal.service.SampleService;
import org.cbioportal.service.exception.MolecularProfileNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class MolecularDataServiceImpl implements MolecularDataService {

    @Autowired
    private MolecularDataRepository molecularDataRepository;
    @Autowired
    private SampleService sampleService;
    @Autowired
    private MolecularProfileService molecularProfileService;
    @Autowired
    private SampleListRepository sampleListRepository;

    @Override
    public List<GeneMolecularData> getMolecularData(String molecularProfileId, String sampleListId,
                                                    List<Integer> entrezGeneIds, String projection)
        throws MolecularProfileNotFoundException {

        validateMolecularProfile(molecularProfileId);
        List<String> sampleIds = sampleListRepository.getAllSampleIdsInSampleList(sampleListId);
        if (sampleIds.isEmpty()) {
            return Collections.emptyList();
        }
        return fetchMolecularData(molecularProfileId, sampleIds, entrezGeneIds, projection);
    }

    @Override
    public BaseMeta getMetaMolecularData(String molecularProfileId, String sampleListId, List<Integer> entrezGeneIds) 
        throws MolecularProfileNotFoundException {

        BaseMeta baseMeta = new BaseMeta();
        baseMeta.setTotalCount(getMolecularData(molecularProfileId, sampleListId, entrezGeneIds, "ID").size());
        return baseMeta;
    }

    @Override
    public List<GeneMolecularData> fetchMolecularData(String molecularProfileId, List<String> sampleIds,
                                                      List<Integer> entrezGeneIds, String projection) 
        throws MolecularProfileNotFoundException {

        validateMolecularProfile(molecularProfileId);
        List<GeneMolecularData> molecularDataList = new ArrayList<>();

        MolecularProfileSamples commaSeparatedSampleIdsOfMolecularProfile = molecularDataRepository
            .getCommaSeparatedSampleIdsOfMolecularProfile(molecularProfileId);
        if (commaSeparatedSampleIdsOfMolecularProfile == null) {
            return molecularDataList;
        }
        List<Integer> internalSampleIds = Arrays.stream(commaSeparatedSampleIdsOfMolecularProfile.getSplitSampleIds())
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

        List<GeneMolecularAlteration> molecularAlterations = molecularDataRepository.getGeneMolecularAlterations(
            molecularProfileId, entrezGeneIds, projection);

        for (Sample sample : samples) {
            Integer indexOfSampleId = internalSampleIdsMap.get(sample.getInternalId());
            if (indexOfSampleId != null) {
                for (GeneMolecularAlteration molecularAlteration : molecularAlterations) {
                    GeneMolecularData molecularData = new GeneMolecularData();
                    molecularData.setMolecularProfileId(molecularProfileId);
                    molecularData.setSampleId(sample.getStableId());
                    molecularData.setPatientId(sample.getPatientStableId());
                    molecularData.setStudyId(sample.getCancerStudyIdentifier());
                    molecularData.setEntrezGeneId(molecularAlteration.getEntrezGeneId());
                    molecularData.setValue(molecularAlteration.getSplitValues()[indexOfSampleId]);
                    molecularData.setGene(molecularAlteration.getGene());
                    molecularDataList.add(molecularData);
                }
            }
        }

        return molecularDataList;
    }

    @Override
    public BaseMeta fetchMetaMolecularData(String molecularProfileId, List<String> sampleIds, 
                                           List<Integer> entrezGeneIds) throws MolecularProfileNotFoundException {

        BaseMeta baseMeta = new BaseMeta();
        baseMeta.setTotalCount(fetchMolecularData(molecularProfileId, sampleIds, entrezGeneIds, "ID").size());
        return baseMeta;
    }

    @Override
    public Iterable<GeneMolecularAlteration> getMolecularAlterations(String molecularProfileId, 
                                                                     List<Integer> entrezGeneIds, String projection)
        throws MolecularProfileNotFoundException {

        validateMolecularProfile(molecularProfileId);
        return molecularDataRepository.getGeneMolecularAlterationsIterable(molecularProfileId, entrezGeneIds, projection);
    }

    @Override
    public Integer getNumberOfSamplesInMolecularProfile(String molecularProfileId) {

        MolecularProfileSamples commaSeparatedSampleIdsOfMolecularProfile = molecularDataRepository
            .getCommaSeparatedSampleIdsOfMolecularProfile(molecularProfileId);
        if (commaSeparatedSampleIdsOfMolecularProfile == null) {
            return null;
        }

        return commaSeparatedSampleIdsOfMolecularProfile.getSplitSampleIds().length;
    }

    @Override
    public List<GeneMolecularData> getMolecularDataInMultipleMolecularProfiles(List<String> molecularProfileIds,
            List<String> sampleIds, List<Integer> entrezGeneIds, String projection) {

        List<GeneMolecularData> molecularDataList = new ArrayList<>();
        List<String> distinctMolecularProfileIds = molecularProfileIds.stream().distinct().sorted().collect(Collectors.toList());

        Map<String, MolecularProfileSamples> commaSeparatedSampleIdsOfMolecularProfilesMap =  molecularDataRepository
                .commaSeparatedSampleIdsOfMolecularProfilesMap(distinctMolecularProfileIds);

        Map<String, Map<Integer, Integer>> internalSampleIdsMap = new HashMap<>();
        List<Integer> allInternalSampleIds = new ArrayList<>();

        for (int i = 0; i < distinctMolecularProfileIds.size(); i++) {
            String molecularProfileId = distinctMolecularProfileIds.get(i);
            List<Integer> internalSampleIds = Arrays
                    .stream(commaSeparatedSampleIdsOfMolecularProfilesMap.get(molecularProfileId).getSplitSampleIds())
                    .mapToInt(Integer::parseInt).boxed().collect(Collectors.toList());
            HashMap<Integer, Integer> molecularProfileSampleMap = new HashMap<Integer, Integer>();
            for (int lc = 0; lc < internalSampleIds.size(); lc++) {
                molecularProfileSampleMap.put(internalSampleIds.get(lc), lc);
            }
            internalSampleIdsMap.put(molecularProfileId, molecularProfileSampleMap);
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

        List<GeneMolecularAlteration> molecularAlterations = molecularDataRepository
            .getGeneMolecularAlterationsInMultipleMolecularProfiles(distinctMolecularProfileIds, entrezGeneIds, projection);
        Map<String, List<GeneMolecularAlteration>> molecularAlterationsMap = molecularAlterations.stream().collect(
            Collectors.groupingBy(GeneMolecularAlteration::getMolecularProfileId));
        
        for (Sample sample : samples) {
            for (MolecularProfile molecularProfile : molecularProfileMapByStudyId.get(sample.getCancerStudyIdentifier())) {
                String molecularProfileId = molecularProfile.getStableId();
                Integer indexOfSampleId = internalSampleIdsMap.get(molecularProfileId).get(sample.getInternalId());
                if (indexOfSampleId != null && molecularAlterationsMap.containsKey(molecularProfileId)) {
                    for (GeneMolecularAlteration molecularAlteration : molecularAlterationsMap.get(molecularProfileId)) {
                        GeneMolecularData molecularData = new GeneMolecularData();
                        molecularData.setMolecularProfileId(molecularProfileId);
                        molecularData.setSampleId(sample.getStableId());
                        molecularData.setPatientId(sample.getPatientStableId());
                        molecularData.setStudyId(sample.getCancerStudyIdentifier());
                        molecularData.setEntrezGeneId(molecularAlteration.getEntrezGeneId());
                        try {
                            molecularData.setValue(molecularAlteration.getSplitValues()[indexOfSampleId]);
                        } catch (ArrayIndexOutOfBoundsException e) {
                            molecularData.setValue(null);
                        }
                        molecularData.setGene(molecularAlteration.getGene());
                        molecularDataList.add(molecularData);
                    }
                }
            }
        }

        return molecularDataList;
    }

    @Override
    @PreAuthorize("hasPermission(#molecularProfileIds, 'Collection<MolecularProfileId>', 'read')")
    public BaseMeta getMetaMolecularDataInMultipleMolecularProfiles(List<String> molecularProfileIds,
            List<String> sampleIds, List<Integer> entrezGeneIds) {

        BaseMeta baseMeta = new BaseMeta();
        baseMeta.setTotalCount(getMolecularDataInMultipleMolecularProfiles(molecularProfileIds, sampleIds, entrezGeneIds, "ID")
            .size());
        return baseMeta;
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
