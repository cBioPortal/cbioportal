package org.cbioportal.service.impl;

import org.cbioportal.model.*;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.persistence.GenePanelRepository;
import org.cbioportal.persistence.MolecularProfileRepository;
import org.cbioportal.service.GenePanelService;
import org.cbioportal.service.SampleListService;
import org.cbioportal.service.exception.GenePanelNotFoundException;
import org.cbioportal.service.exception.MolecularProfileNotFoundException;
import org.cbioportal.service.exception.SampleListNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

@Service
public class GenePanelServiceImpl implements GenePanelService {
    @Autowired
    private GenePanelRepository genePanelRepository;
    @Autowired
    private MolecularProfileRepository molecularProfileRepository;
    @Autowired
    private SampleListService sampleListService;

    private static final String SEQUENCED_LIST_SUFFIX = "_sequenced";

    Function<GenePanelData, String> sampleUniqueIdentifier = sample -> sample.getMolecularProfileId() + sample.getSampleId();
    Function<GenePanelData, String> patientUniqueIdentifier = sample -> sample.getMolecularProfileId() + sample.getPatientId();

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

        Stream<GenePanelData> genePanelDataStream = genePanelRepository
            .getGenePanelDataBySampleListId(molecularProfileId.replace("_fusion", "_mutations"), sampleListId)
            .stream();

        // TODO: remove this block after fusion are migrated to structural variant in database
        if (molecularProfileId.endsWith("_fusion")) {
            genePanelDataStream = genePanelDataStream
                .map(datum -> transformMutationToFusionPanelData(molecularProfileId, datum));
        }

        return annotateDataFromSequencedSampleLists(
            genePanelDataStream,
            Collections.singleton(molecularProfileId));
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

        Map<String, List<GenePanelData>> molecularProfileIdToGenePanelDataMap = molecularProfileIds
            .stream()
            .map(profileId -> profileId.replace("_fusion", "_mutations"))
            .distinct()
            //query database with each profile id so data cached in a modular way for each profile 
            .collect(Collectors.toMap(Function.identity(), profileId -> genePanelRepository
                .fetchGenePanelDataByMolecularProfileId(profileId)));

        return molecularProfileIds
            .stream()
            .flatMap(profileId -> {
                if (profileId.endsWith("_fusion")) {
                    return molecularProfileIdToGenePanelDataMap
                        .getOrDefault(profileId.replace("_fusion", "_mutations"), new ArrayList<>())
                        .stream()
                        .map(datum -> transformMutationToFusionPanelData(profileId, datum));
                } else {
                    return molecularProfileIdToGenePanelDataMap
                        .getOrDefault(profileId.replace("_fusion", "_mutations"), new ArrayList<>())
                        .stream();
                }
            })
            .collect(Collectors.toList());
    }

    @Override
    public List<GenePanelData> fetchGenePanelDataInMultipleMolecularProfiles(List<MolecularProfileCaseIdentifier> molecularProfileSampleIdentifiers) {
        return getGenePanelData(molecularProfileSampleIdentifiers, sampleUniqueIdentifier);
    }

    @Override
    public List<GenePanelData> fetchGenePanelDataInMultipleMolecularProfilesByPatientIds(List<MolecularProfileCaseIdentifier> molecularProfilePatientIdentifiers) {
        return getGenePanelData(molecularProfilePatientIdentifiers, patientUniqueIdentifier);
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

        Stream<GenePanelData> genePanelDataStream = fetchGenePanelDataByMolecularProfileIds(molecularProfileIds)
            .stream()
            .filter(datum -> queriedMolecularProfileCaseIdentifierMap.containsKey(keyGenerator.apply(datum)));

        return annotateDataFromSequencedSampleLists(genePanelDataStream, molecularProfileIds);
    }

    private List<GenePanelData> annotateDataFromSequencedSampleLists(Stream<GenePanelData> genePanelData,
                                                                     Set<String> molecularProfileIds) {

        Map<String, MolecularProfile> sequencedMolecularProfileMap = molecularProfileRepository
            .getMolecularProfiles(new ArrayList<>(molecularProfileIds), "SUMMARY")
            .stream()
            .filter(profile ->
                profile.getMolecularAlterationType().equals(MolecularProfile.MolecularAlterationType.MUTATION_EXTENDED) ||
                    profile.getMolecularAlterationType().equals(MolecularProfile.MolecularAlterationType.FUSION) ||
                    (profile.getMolecularAlterationType().equals(MolecularProfile.MolecularAlterationType.STRUCTURAL_VARIANT) &&
                        profile.getDatatype().equalsIgnoreCase("FUSION")))
            .collect(Collectors.toMap(MolecularProfile::getStableId, Function.identity()));

        Map<String, Boolean> sampleSequencedBySampleList = new HashMap<>();

        sequencedMolecularProfileMap
            .values()
            .stream()
            .map(profile -> profile.getCancerStudyIdentifier() + SEQUENCED_LIST_SUFFIX)
            .distinct()
            .forEach(sampleListId -> {
                try {
                    SampleList sampleList = sampleListService.getSampleList(sampleListId);
                    sampleList
                        .getSampleIds()
                        .forEach(sampleId ->
                            sampleSequencedBySampleList.put(sampleList.getCancerStudyIdentifier() + sampleId, true));
                } catch (SampleListNotFoundException ignored) {
                }
            });

        if (!sampleSequencedBySampleList.isEmpty()) {
            return genePanelData
                .map(datum -> {
                    if (!datum.getProfiled() && sequencedMolecularProfileMap.containsKey(datum.getMolecularProfileId())) {
                        datum.setProfiled(sampleSequencedBySampleList.getOrDefault(datum.getStudyId() + datum.getSampleId(), false));
                    }
                    return datum;
                })
                .collect(toList());
        }

        return genePanelData.collect(toList());
    }

    private GenePanelData transformMutationToFusionPanelData(String molecularProfileId, GenePanelData mutationPanelData) {
        GenePanelData fusionPanelData = new GenePanelData();
        fusionPanelData.setMolecularProfileId(molecularProfileId);
        fusionPanelData.setSampleId(mutationPanelData.getSampleId());
        fusionPanelData.setPatientId(mutationPanelData.getPatientId());
        fusionPanelData.setStudyId(mutationPanelData.getStudyId());
        fusionPanelData.setProfiled(mutationPanelData.getProfiled());
        fusionPanelData.setGenePanelId(mutationPanelData.getGenePanelId());
        return fusionPanelData;
    }

}
