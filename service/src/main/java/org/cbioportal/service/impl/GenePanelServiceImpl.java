package org.cbioportal.service.impl;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.*;

import org.apache.commons.math3.util.Pair;
import org.cbioportal.model.*;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.persistence.GenePanelRepository;
import org.cbioportal.persistence.MolecularProfileRepository;
import org.cbioportal.persistence.StudyRepository;
import org.cbioportal.service.GenePanelService;
import org.cbioportal.service.exception.GenePanelNotFoundException;
import org.cbioportal.service.exception.MolecularProfileNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GenePanelServiceImpl implements GenePanelService {
    @Autowired
    private GenePanelRepository genePanelRepository;
    @Autowired
    private MolecularProfileRepository molecularProfileRepository;
    @Autowired
    private StudyRepository studyRepository;

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

        return genePanelRepository.getGenePanelDataBySampleListId(molecularProfileId, sampleListId);
    }

    @Override
    public List<GenePanelData> fetchGenePanelData(String molecularProfileId, List<String> sampleIds)
        throws MolecularProfileNotFoundException {

        return genePanelRepository.fetchGenePanelData(molecularProfileId, sampleIds);
    }

    @Override
    public List<GenePanelData> fetchGenePanelDataByMolecularProfileIds(Set<String> molecularProfileIds) {

        AtomicReference<Boolean> hasFusions = new AtomicReference<>(false);

        Set<String> updatedMolecularProfileIds = molecularProfileIds
            .stream()
            .map(molecularProfileId -> {
                if (molecularProfileId.endsWith("_fusion")) {
                    hasFusions.set(true);
                    return molecularProfileId.replace("_fusion", "_mutations");
                }
                return molecularProfileId;
            }).collect(Collectors.toSet());

        List<GenePanelData> genePanelData = updatedMolecularProfileIds
            .stream()
            //query database with each profile id so data cached in a modular way for each profile 
            .flatMap(profileId -> genePanelRepository.fetchGenePanelDataByMolecularProfileId(profileId).stream())
            .collect(Collectors.toList());

        // TODO: remove this block after fusion are migrated to structural variant in database
        if (hasFusions.get()) {
            Map<String, List<GenePanelData>> genePanelDataByProfileId = genePanelData
                .stream()
                .collect(Collectors.groupingBy(GenePanelData::getMolecularProfileId));
            return molecularProfileIds.stream().flatMap(molecularProfileId -> {
                if (molecularProfileId.endsWith("_fusion")) {
                    return genePanelDataByProfileId
                        .getOrDefault(molecularProfileId.replace("_fusion", "_mutations"), new ArrayList<>())
                        .stream().map(datum -> transformMutationToFusionPanelData(molecularProfileId, datum));
                }
                return genePanelDataByProfileId.getOrDefault(molecularProfileId, new ArrayList<>()).stream();
            }).collect(Collectors.toList());
        } else {
            return genePanelData;
        }
    }

    @Override
    public List<GenePanelData> fetchGenePanelDataInMultipleMolecularProfiles(List<MolecularProfileCaseIdentifier> molecularProfileSampleIdentifiers) {

        // First object: whether to filter data or not
        // Second object: data
        Pair<Boolean, List<GenePanelData>> result =
            getGenePanelData(
                molecularProfileSampleIdentifiers,
                ids -> genePanelRepository.fetchGenePanelDataInMultipleMolecularProfiles(ids));

        // check if data needs to be filtered, if not return complete data
        if (!result.getFirst()) {
            return result.getSecond();
        }

        Map<Pair<String, String>, GenePanelData> genePanelDataSet = result.getSecond()
            .stream()
            .collect(toMap(
                datum -> new Pair<>(datum.getMolecularProfileId(), datum.getSampleId()), Function.identity()));
        List<GenePanelData> genePanelData = new ArrayList<>();

        for (MolecularProfileCaseIdentifier profileCaseIdentifier : molecularProfileSampleIdentifiers) {
            String molecularProfileId = profileCaseIdentifier.getMolecularProfileId();
            String sampleId = profileCaseIdentifier.getCaseId();
            Pair<String, String> key = new Pair<>(molecularProfileId, sampleId);
            if (genePanelDataSet.containsKey(key)) {
                genePanelData.add(genePanelDataSet.get(key));
            } else if (molecularProfileId.endsWith("_fusion")) {
                // TODO: remove this block after fusion are migrated to structural variant in database
                String mutationMolecularProfileId = molecularProfileId.replace("_fusion", "_mutations");
                key = new Pair<>(mutationMolecularProfileId, sampleId);
                if (genePanelDataSet.containsKey(key)) {
                    genePanelData.add(transformMutationToFusionPanelData(molecularProfileId, genePanelDataSet.get(key)));
                }
            }
        }
        return genePanelData;
    }

    @Override
    public List<GenePanelData> fetchGenePanelDataInMultipleMolecularProfilesByPatientIds(List<MolecularProfileCaseIdentifier> molecularProfilePatientIdentifiers) {

        // First object: whether to filter data or not
        // Second object: data
        Pair<Boolean, List<GenePanelData>> result =
            getGenePanelData(
                molecularProfilePatientIdentifiers,
                ids -> genePanelRepository.fetchGenePanelDataInMultipleMolecularProfilesByPatientIds(ids));

        // check if data needs to be filtered, if not return complete data
        if (!result.getFirst()) {
            return result.getSecond();
        }

        Map<Object, List<GenePanelData>> genePanelDataSet = result.getSecond()
            .stream()
            .collect(groupingBy(datum -> new Pair<>(datum.getMolecularProfileId(), datum.getPatientId())));
        List<GenePanelData> genePanelData = new ArrayList<>();

        for (MolecularProfileCaseIdentifier profileCaseIdentifier : molecularProfilePatientIdentifiers) {
            String molecularProfileId = profileCaseIdentifier.getMolecularProfileId();
            String patientId = profileCaseIdentifier.getCaseId();
            Pair<String, String> key = new Pair<>(molecularProfileId, patientId);
            if (genePanelDataSet.containsKey(key)) {
                genePanelData.addAll(genePanelDataSet.get(key));
            } else if (molecularProfileId.endsWith("_fusion")) {
                // TODO: remove this block after fusion are migrated to structural variant in database
                String mutationMolecularProfileId = molecularProfileId.replace("_fusion", "_mutations");
                key = new Pair<>(mutationMolecularProfileId, patientId);
                for (GenePanelData mutationPanelData : genePanelDataSet.getOrDefault(key, new ArrayList<>())) {
                    genePanelData.add(transformMutationToFusionPanelData(molecularProfileId, mutationPanelData));
                }
            }
        }
        return genePanelData;
    }

    private Pair<Boolean, List<GenePanelData>> getGenePanelData(List<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers, Function<List<MolecularProfileCaseIdentifier>, List<GenePanelData>> queryFunction) {

        AtomicReference<Boolean> hasFusions = new AtomicReference<>(false);
        Set<String> molecularProfileIds = new HashSet<>();

        Set<MolecularProfileCaseIdentifier> updatedMolecularProfileCaseIdentifiers = molecularProfileCaseIdentifiers
            .stream()
            .map(molecularProfileSampleIdentifier -> {
                molecularProfileIds.add(molecularProfileSampleIdentifier.getMolecularProfileId());
                if (molecularProfileSampleIdentifier.getMolecularProfileId().endsWith("_fusion")) {
                    hasFusions.set(true);
                    MolecularProfileCaseIdentifier profileCaseIdentifier = new MolecularProfileCaseIdentifier();
                    profileCaseIdentifier.setMolecularProfileId(molecularProfileSampleIdentifier.getMolecularProfileId().replace("_fusion", "_mutations"));
                    profileCaseIdentifier.setCaseId(molecularProfileSampleIdentifier.getCaseId());
                    return profileCaseIdentifier;
                }
                return molecularProfileSampleIdentifier;
            }).collect(Collectors.toSet());

        List<MolecularProfile> molecularProfiles = molecularProfileRepository.getMolecularProfiles(new ArrayList<>(molecularProfileIds), "SUMMARY");

        List<String> studyIds = molecularProfiles
            .stream()
            .map(MolecularProfile::getCancerStudyIdentifier)
            .distinct()
            .collect(Collectors.toList());

        int totalSampleCount = studyRepository.fetchStudies(studyIds, "SUMMARY")
            .stream()
            .mapToInt(CancerStudy::getAllSampleCount)
            .sum();

        // fetch all data for molecular profiles if the sampleIdentifiers are more than 30% of total sample in queried studies
        if (updatedMolecularProfileCaseIdentifiers.size() > totalSampleCount * 0.3) {
            return new Pair<>(true, fetchGenePanelDataByMolecularProfileIds(molecularProfileIds));
        } else {
            return new Pair<>(hasFusions.get(), queryFunction.apply(new ArrayList<>(updatedMolecularProfileCaseIdentifiers)));
        }
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
