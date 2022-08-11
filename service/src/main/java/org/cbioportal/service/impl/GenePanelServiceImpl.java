package org.cbioportal.service.impl;

import org.apache.commons.collections4.CollectionUtils;
import org.cbioportal.model.*;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.persistence.GenePanelRepository;
import org.cbioportal.service.GenePanelService;
import org.cbioportal.service.MolecularProfileService;
import org.cbioportal.service.SampleListService;
import org.cbioportal.service.exception.GenePanelNotFoundException;
import org.cbioportal.service.exception.MolecularProfileNotFoundException;
import org.cbioportal.service.exception.SampleListNotFoundException;
import org.cbioportal.service.util.MolecularProfileUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.*;

@Service
public class GenePanelServiceImpl implements GenePanelService {
    @Autowired
    private GenePanelRepository genePanelRepository;
    @Autowired
    private MolecularProfileService molecularProfileService;
    @Autowired
    private SampleListService sampleListService;
    @Autowired
    private MolecularProfileUtil molecularProfileUtil;

    private final String SEQUENCED_LIST_SUFFIX = "_sequenced";

    private final Function<GenePanelData, String> SAMPLE_IDENTIFIER_GENERATOR = d -> d.getMolecularProfileId() + d.getSampleId();
    private final Function<GenePanelData, String> PATIENT_IDENTIFIER_GENERATOR = d -> d.getMolecularProfileId() + d.getPatientId();

    @Override
    public List<GenePanel> getAllGenePanels(String projection, Integer pageSize, Integer pageNumber, String sortBy, 
                                            String direction) {

        List<GenePanel> genePanels = genePanelRepository.getAllGenePanels(projection, pageSize, pageNumber, sortBy, 
            direction);

        if (projection.equals("DETAILED")) {

            List<GenePanelToGene> genePanelToGeneList = genePanelRepository.getGenesOfPanels(genePanels
                .stream().map(GenePanel::getStableId).collect(toList()));

            genePanels.forEach(g -> g.setGenes(genePanelToGeneList.stream().filter(p -> p.getGenePanelId()
                .equals(g.getStableId())).collect(toList())));
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

        genePanel.setGenes(genePanelRepository.getGenesOfPanels(Collections.singletonList(genePanelId)));
        return genePanel;
    }

    @Override
    public List<GenePanel> fetchGenePanels(List<String> genePanelIds, String projection) {

        List<GenePanel> genePanels = genePanelRepository.fetchGenePanels(genePanelIds, projection);

        if (projection.equals("DETAILED")) {

            List<GenePanelToGene> genePanelToGeneList = genePanelRepository.getGenesOfPanels(genePanels
                .stream().map(GenePanel::getStableId).collect(toList()));

            genePanels.forEach(g -> g.setGenes(genePanelToGeneList.stream().filter(p -> p.getGenePanelId()
                .equals(g.getStableId())).collect(toList())));
        }

        return genePanels;
    }

    @Override
    public List<GenePanelData> getGenePanelData(String molecularProfileId, String sampleListId)
        throws MolecularProfileNotFoundException {

        MolecularProfile molecularProfile = molecularProfileService.getMolecularProfile(molecularProfileId);

        List<GenePanelData> genePanelData = genePanelRepository
            .getGenePanelDataBySampleListId(molecularProfile.getStableId(), sampleListId);

        return annotateDataFromSequencedSampleLists(genePanelData, molecularProfile);
    }

    @Override
    public List<GenePanelData> fetchGenePanelData(String molecularProfileId, List<String> sampleIds)
        throws MolecularProfileNotFoundException {

        List<MolecularProfileCaseIdentifier> molecularProfileSampleIdentifiers = sampleIds
            .stream()
            .map(sampleId -> new MolecularProfileCaseIdentifier(sampleId, molecularProfileId))
            .collect(toList());

        return fetchGenePanelDataInMultipleMolecularProfiles(molecularProfileSampleIdentifiers);
    }

    @Override
    public List<GenePanelData> fetchGenePanelDataByMolecularProfileIds(Set<String> molecularProfileIds) {

        List<MolecularProfile> molecularProfiles = molecularProfileService
            .getMolecularProfiles(molecularProfileIds, "SUMMARY");

        Set<String> uniqueMolecularProfileIds = molecularProfiles
            .stream()
            .map(MolecularProfile::getStableId)
            .collect(toSet());

        Map<String, List<GenePanelData>> molecularProfileIdToGenePanelDataMap = uniqueMolecularProfileIds
            .stream()
            //query database with each profile id so data cached in a modular way for each profile 
            .collect(Collectors.toMap(Function.identity(), profileId -> genePanelRepository
                .fetchGenePanelDataByMolecularProfileId(profileId)));

        Map<String,MolecularProfile> molecularProfileIdMap = molecularProfiles
            .stream()
            .collect(Collectors.toMap(MolecularProfile::getStableId, Function.identity()));

        return molecularProfileIds
            .stream()
            .flatMap(profileId -> {
                List<GenePanelData> genePanelData = molecularProfileIdToGenePanelDataMap
                    .getOrDefault(profileId, new ArrayList<>());
                if (CollectionUtils.isNotEmpty(genePanelData)) {
                    genePanelData = annotateDataFromSequencedSampleLists(genePanelData, molecularProfileIdMap.get(profileId));
                }
                return genePanelData.stream();
            })
            .collect(Collectors.toList());
    }

    @Override
    public List<GenePanelData> fetchGenePanelDataInMultipleMolecularProfiles(List<MolecularProfileCaseIdentifier> molecularProfileSampleIdentifiers) {
        return getGenePanelData(molecularProfileSampleIdentifiers, SAMPLE_IDENTIFIER_GENERATOR);
    }

    @Override
    public List<GenePanelData> fetchGenePanelDataInMultipleMolecularProfilesByPatientIds(List<MolecularProfileCaseIdentifier> molecularProfilePatientIdentifiers) {
        return getGenePanelData(molecularProfilePatientIdentifiers, PATIENT_IDENTIFIER_GENERATOR);
    }

    private List<GenePanelData> getGenePanelData(List<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers,
                                                 Function<GenePanelData, String> keyGenerator) {
        Set<String> molecularProfileIds = molecularProfileCaseIdentifiers
            .stream()
            .map(MolecularProfileCaseIdentifier::getMolecularProfileId)
            .collect(toSet());

        Map<String, Boolean> queriedMolecularProfileCaseIdentifierMap =
            molecularProfileCaseIdentifiers
                .stream()
                .collect(Collectors
                    .toMap(
                        datum -> datum.getMolecularProfileId() + datum.getCaseId(),
                        datum -> true));

        return fetchGenePanelDataByMolecularProfileIds(molecularProfileIds)
            .stream()
            .filter(datum -> queriedMolecularProfileCaseIdentifierMap.containsKey(keyGenerator.apply(datum)))
            .collect(toList());

    }

    /**
     * For mutation use sequenced case/sample list to check if the sample are profiled or not 
     * @param genePanelData list of gene panel data objects
     * @param molecularProfile MolecularProfile
     * @return List of GenePanelData
     */
    private List<GenePanelData> annotateDataFromSequencedSampleLists(List<GenePanelData> genePanelData,
                                                                     MolecularProfile molecularProfile) {

        if (MolecularProfile.MolecularAlterationType.MUTATION_EXTENDED.equals(molecularProfile.getMolecularAlterationType())) {
            try {
                SampleList sampleList = sampleListService.getSampleList(molecularProfile.getCancerStudyIdentifier() + SEQUENCED_LIST_SUFFIX);
                Map<String, Boolean> sampleSequencedBySampleList = sampleList
                    .getSampleIds()
                    .stream()
                    .collect(toMap(Function.identity(), d -> true));
                return genePanelData
                    .stream()
                    .peek(datum -> {
                        if(!datum.getProfiled()) {
                            datum.setProfiled(sampleSequencedBySampleList.getOrDefault(datum.getSampleId(), false));
                        }
                    })
                    .collect(toList());

            } catch (SampleListNotFoundException ignored) {
            }
        }
        return genePanelData;
    }

}
