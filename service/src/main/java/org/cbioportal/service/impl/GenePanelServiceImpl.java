package org.cbioportal.service.impl;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.*;

import org.apache.commons.math3.util.Pair;
import org.cbioportal.model.GenePanel;
import org.cbioportal.model.GenePanelData;
import org.cbioportal.model.GenePanelToGene;
import org.cbioportal.model.MolecularProfileCaseIdentifier;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.persistence.GenePanelRepository;
import org.cbioportal.service.GenePanelService;
import org.cbioportal.service.exception.GenePanelNotFoundException;
import org.cbioportal.service.exception.MolecularProfileNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GenePanelServiceImpl implements GenePanelService {
    @Autowired
    private GenePanelRepository genePanelRepository;
    
    private final Integer maxCasesCountToIncludeInQuery = 30000;

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
    public List<GenePanelData> fetchGenePanelDataInMultipleMolecularProfiles(List<MolecularProfileCaseIdentifier> molecularProfileSampleIdentifiers) {

        AtomicReference<Boolean> hasFusions = new AtomicReference<>(false);

        // TODO: remove this block after fusion are migrated to structural variant in database
        Set<MolecularProfileCaseIdentifier> updatedMolecularProfileSampleIdentifiers = molecularProfileSampleIdentifiers.stream().map(molecularProfileSampleIdentifier -> {
            if (molecularProfileSampleIdentifier.getMolecularProfileId().endsWith("_fusion")) {
                hasFusions.set(true);
                MolecularProfileCaseIdentifier profileCaseIdentifier = new MolecularProfileCaseIdentifier();
                profileCaseIdentifier.setMolecularProfileId(molecularProfileSampleIdentifier.getMolecularProfileId().replace("_fusion", "_mutations"));
                profileCaseIdentifier.setCaseId(molecularProfileSampleIdentifier.getCaseId());
                return profileCaseIdentifier;
            }
            return molecularProfileSampleIdentifier;
        }).collect(Collectors.toSet());
        // TODO: remove this block after fusion are migrated to structural variant in database

        List<GenePanelData> genePanelDataForQueriedProfiles;
        if (updatedMolecularProfileSampleIdentifiers.size() < maxCasesCountToIncludeInQuery) {
            genePanelDataForQueriedProfiles = genePanelRepository.fetchGenePanelDataInMultipleMolecularProfiles(new ArrayList<>(updatedMolecularProfileSampleIdentifiers));
            if (!hasFusions.get()) {
                //return response that is directly coming from database query are samples are already filtered
                return genePanelDataForQueriedProfiles;
            }
        } else {
            List<String> molecularProfileIds = updatedMolecularProfileSampleIdentifiers
                .stream()
                .map(MolecularProfileCaseIdentifier::getMolecularProfileId)
                .distinct()
                .collect(toList());
            genePanelDataForQueriedProfiles = genePanelRepository.fetchGenePanelDataByMolecularProfileIds(molecularProfileIds);
        }

        Map<Pair<String, String>, GenePanelData> genePanelDataSet = genePanelDataForQueriedProfiles
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
                    GenePanelData mutationPanelData = genePanelDataSet.get(key);
                    GenePanelData fusionPanelData = new GenePanelData();
                    fusionPanelData.setMolecularProfileId(molecularProfileId);
                    fusionPanelData.setSampleId(mutationPanelData.getSampleId());
                    fusionPanelData.setPatientId(mutationPanelData.getPatientId());
                    fusionPanelData.setStudyId(mutationPanelData.getStudyId());
                    fusionPanelData.setProfiled(mutationPanelData.getProfiled());
                    fusionPanelData.setGenePanelId(mutationPanelData.getGenePanelId());
                    genePanelData.add(fusionPanelData);
                }
            }
        }
        return genePanelData;
    }

    @Override
    public List<GenePanelData> fetchGenePanelDataInMultipleMolecularProfilesByPatientIds(List<MolecularProfileCaseIdentifier> molecularProfilePatientIdentifiers) {

        AtomicReference<Boolean> hasFusions = new AtomicReference<>(false);

        // TODO: remove this block after fusion are migrated to structural variant in database
        Set<MolecularProfileCaseIdentifier> updatedMolecularProfilePatientIdentifiers = molecularProfilePatientIdentifiers.stream().map(molecularProfileSampleIdentifier -> {
            if (molecularProfileSampleIdentifier.getMolecularProfileId().endsWith("_fusion")) {
                hasFusions.set(true);
                MolecularProfileCaseIdentifier profileCaseIdentifier = new MolecularProfileCaseIdentifier();
                profileCaseIdentifier.setMolecularProfileId(molecularProfileSampleIdentifier.getMolecularProfileId().replace("_fusion", "_mutations"));
                profileCaseIdentifier.setCaseId(molecularProfileSampleIdentifier.getCaseId());
                return profileCaseIdentifier;
            }
            return molecularProfileSampleIdentifier;
        }).collect(Collectors.toSet());
        // TODO: remove this block after fusion are migrated to structural variant in database

        List<GenePanelData> genePanelDataForQueriedProfiles;
        if (updatedMolecularProfilePatientIdentifiers.size() < maxCasesCountToIncludeInQuery) {
            genePanelDataForQueriedProfiles = genePanelRepository.fetchGenePanelDataInMultipleMolecularProfilesByPatientIds(new ArrayList<>(updatedMolecularProfilePatientIdentifiers));
            if (!hasFusions.get()) {
                //return response that is directly coming from database query are samples are already filtered
                return genePanelDataForQueriedProfiles;
            }
        } else {
            List<String> molecularProfileIds = updatedMolecularProfilePatientIdentifiers
                .stream()
                .map(MolecularProfileCaseIdentifier::getMolecularProfileId)
                .distinct()
                .collect(toList());
            genePanelDataForQueriedProfiles = genePanelRepository.fetchGenePanelDataByMolecularProfileIds(molecularProfileIds);
        }

        Map<Object, List<GenePanelData>> genePanelDataSet = genePanelDataForQueriedProfiles
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
                    GenePanelData fusionPanelData = new GenePanelData();
                    fusionPanelData.setMolecularProfileId(molecularProfileId);
                    fusionPanelData.setSampleId(mutationPanelData.getSampleId());
                    fusionPanelData.setPatientId(mutationPanelData.getPatientId());
                    fusionPanelData.setStudyId(mutationPanelData.getStudyId());
                    fusionPanelData.setProfiled(mutationPanelData.getProfiled());
                    fusionPanelData.setGenePanelId(mutationPanelData.getGenePanelId());
                    genePanelData.add(fusionPanelData);
                }
            }
        }
        return genePanelData;
    }

}
