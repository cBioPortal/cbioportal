package org.cbioportal.service.impl;

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

        // replace fusion profile id with mutation, as all the fusions are in mutation table
        // and are imported with mutation profile id
        // TODO: remove replacing logic once the fusions are migrated to structural variant in database
        MolecularProfile molecularProfile = molecularProfileService
            .getMolecularProfile(molecularProfileUtil.replaceFusionProfileWithMutationProfile(molecularProfileId));

        List<GenePanelData> genePanelData = genePanelRepository
            .getGenePanelDataBySampleListId(molecularProfile.getStableId(), sampleListId);

        genePanelData = annotateDataFromSequencedSampleLists(genePanelData, molecularProfile);

        // TODO: remove this block after fusion are migrated to structural variant in database
        if (molecularProfileId.endsWith(molecularProfileUtil.FUSION_PROFILE_SUFFIX)) {
            genePanelData = genePanelData
                .stream()
                .map(datum -> transformMutationToFusionPanelData(molecularProfileId, datum))
                .collect(toList());
        }

        return genePanelData;
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

        // replace fusion profile id with mutation, as all the fusions are in mutation table
        // and are imported with mutation profile id
        // TODO: remove replacing logic once the fusions are migrated to structural variant in database
        List<String> uniqueMolecularProfileIds = molecularProfileIds
            .stream()
            .map(profileId -> molecularProfileUtil.replaceFusionProfileWithMutationProfile(profileId))
            .distinct()
            .collect(toList());

        Map<String, List<GenePanelData>> molecularProfileIdToGenePanelDataMap = molecularProfileService
            .getMolecularProfiles(uniqueMolecularProfileIds, "SUMMARY")
            .stream()
            //query database with each profile id so data cached in a modular way for each profile 
            .collect(Collectors.toMap(MolecularProfile::getStableId, molecularProfile -> {
                List<GenePanelData> data = genePanelRepository
                    .fetchGenePanelDataByMolecularProfileId(molecularProfile.getStableId());
                return annotateDataFromSequencedSampleLists(data, molecularProfile);
            }));

        return molecularProfileIds
            .stream()
            .flatMap(profileId -> {
                // TODO: remove replacing logic once the fusions are migrated to structural variant in database
                if (profileId.endsWith(molecularProfileUtil.FUSION_PROFILE_SUFFIX)) {
                    return molecularProfileIdToGenePanelDataMap
                        .getOrDefault(molecularProfileUtil.replaceFusionProfileWithMutationProfile(profileId), new ArrayList<>())
                        .stream()
                        .map(datum -> transformMutationToFusionPanelData(profileId, datum));
                } else {
                    return molecularProfileIdToGenePanelDataMap
                        .getOrDefault(profileId, new ArrayList<>())
                        .stream();
                }
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
     * For mutation and fusion profile use sequenced case/sample list to check if the sample are profiled or not 
     * @param genePanelData list of gene panel data objects
     * @param molecularProfile MolecularProfile
     * @return List of GenePanelData
     */
    private List<GenePanelData> annotateDataFromSequencedSampleLists(List<GenePanelData> genePanelData,
                                                                     MolecularProfile molecularProfile) {

        if (MolecularProfile.MolecularAlterationType.MUTATION_EXTENDED.equals(molecularProfile.getMolecularAlterationType()) ||
            MolecularProfile.MolecularAlterationType.FUSION.equals(molecularProfile.getMolecularAlterationType()) ||
            (MolecularProfile.MolecularAlterationType.STRUCTURAL_VARIANT.equals(molecularProfile.getMolecularAlterationType()) &&
                molecularProfile.getDatatype().equalsIgnoreCase("FUSION"))) {
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
