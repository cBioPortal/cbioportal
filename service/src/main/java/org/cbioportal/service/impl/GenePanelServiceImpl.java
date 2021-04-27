package org.cbioportal.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.*;

import org.apache.commons.math3.util.Pair;
import org.cbioportal.model.GenePanel;
import org.cbioportal.model.GenePanelData;
import org.cbioportal.model.GenePanelToGene;
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

        genePanel.setGenes(genePanelRepository.getGenesOfPanels(Arrays.asList(genePanelId)));
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
    public List<GenePanelData> fetchGenePanelDataInMultipleMolecularProfiles(List<String> molecularProfileIds,
                                                                             List<String> sampleIds) {

        List<String> distinctMolecularProfileIds = molecularProfileIds.stream().distinct().collect(Collectors.toList());

        boolean hasFusionProfileIdsInQuery = distinctMolecularProfileIds
            .stream()
            .anyMatch(molecularProfileId -> molecularProfileId.endsWith("_fusion"));

        List<GenePanelData> genePanelData = new ArrayList<>();
        List<GenePanelData> genePanelDataForQueriedProfiles = new ArrayList<>();

        if (!hasFusionProfileIdsInQuery) {
            if(sampleIds.size() < maxCasesCountToIncludeInQuery) {
                //return response that is directly coming from database query are samples are already filtered
                return genePanelRepository.fetchGenePanelDataInMultipleMolecularProfiles(molecularProfileIds, sampleIds);
            } else {
                genePanelDataForQueriedProfiles = genePanelRepository.fetchGenePanelDataInMultipleMolecularProfiles(distinctMolecularProfileIds, null);
            }
        } else {
            // TODO: remove this block after fusion are migrated to structural variant in database

            List<String> updatedMolecularProfileIds = new ArrayList<>(molecularProfileIds);
            List<String> updatedSampleIds = new ArrayList<>(sampleIds);

            for (int i = 0; i < molecularProfileIds.size(); i++) {
                String molecularProfileId = molecularProfileIds.get(i);
                if(molecularProfileId.endsWith("_fusion")) {
                    updatedMolecularProfileIds.add(molecularProfileId.replace("_fusion", "_mutations"));
                    updatedSampleIds.add(sampleIds.get(i));
                }
            }

            if(updatedSampleIds.size() < maxCasesCountToIncludeInQuery) {
                genePanelDataForQueriedProfiles = genePanelRepository
                    .fetchGenePanelDataInMultipleMolecularProfiles(updatedMolecularProfileIds, updatedSampleIds);
            } else {
                distinctMolecularProfileIds = updatedMolecularProfileIds
                    .stream()
                    .distinct()
                    .collect(Collectors.toList());
                genePanelDataForQueriedProfiles = genePanelRepository.fetchGenePanelDataInMultipleMolecularProfiles(distinctMolecularProfileIds, null);
            }
        }

        Map<Pair<String, String>, GenePanelData> genePanelDataSet = genePanelDataForQueriedProfiles
            .stream()
            .collect(toMap(
                datum -> new Pair(datum.getMolecularProfileId(),
                    datum.getSampleId()), Function.identity()));

        for (int i = 0; i < molecularProfileIds.size(); i++) {
            String molecularProfileId = molecularProfileIds.get(i);
            String sampleId = sampleIds.get(i);
            if(molecularProfileId.endsWith("_fusion")) {
                String mutationMolecularProfileId = molecularProfileId.replace("_fusion", "_mutations");
                Pair<String, String> key = new Pair(mutationMolecularProfileId, sampleId);
                if(genePanelDataSet.containsKey(key)) {
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
            } else {
                Pair<String, String> key = new Pair(molecularProfileId, sampleId);
                if(genePanelDataSet.containsKey(key)) {
                    genePanelData.add(genePanelDataSet.get(key));
                }
            }
        }
        return genePanelData;
    }
    
    @Override
    public List<GenePanelData> fetchGenePanelDataInMultipleMolecularProfilesByPatientIds(List<String> molecularProfileIds,
                                                                             List<String> patientIds) {

        List<String> distinctMolecularProfileIds = molecularProfileIds.stream().distinct().collect(Collectors.toList());

        boolean hasFusionProfileIdsInQuery = distinctMolecularProfileIds
            .stream()
            .anyMatch(molecularProfileId -> molecularProfileId.endsWith("_fusion"));

        List<GenePanelData> genePanelData = new ArrayList<>();
        List<GenePanelData> genePanelDataForQueriedProfiles = new ArrayList<>();

        if (!hasFusionProfileIdsInQuery) {
            if(patientIds.size() < maxCasesCountToIncludeInQuery) {
                //return response that is directly coming from database query are samples are already filtered
                return genePanelRepository.fetchGenePanelDataInMultipleMolecularProfilesByPatientIds(molecularProfileIds, patientIds);
            } else {
                genePanelDataForQueriedProfiles = genePanelRepository.fetchGenePanelDataInMultipleMolecularProfilesByPatientIds(distinctMolecularProfileIds, null);
            }
        } else {
            // TODO: remove this block after fusion are migrated to structural variant in database

            List<String> updatedMolecularProfileIds = new ArrayList<>(molecularProfileIds);
            List<String> updatedPatientIds = new ArrayList<>(patientIds);

            for (int i = 0; i < molecularProfileIds.size(); i++) {
                String molecularProfileId = molecularProfileIds.get(i);
                if(molecularProfileId.endsWith("_fusion")) {
                    updatedMolecularProfileIds.add(molecularProfileId.replace("_fusion", "_mutations"));
                    updatedPatientIds.add(patientIds.get(i));
                }
            }

            if(updatedPatientIds.size() < maxCasesCountToIncludeInQuery) {
                genePanelDataForQueriedProfiles = genePanelRepository
                    .fetchGenePanelDataInMultipleMolecularProfilesByPatientIds(updatedMolecularProfileIds, updatedPatientIds);
            } else {
                distinctMolecularProfileIds = updatedMolecularProfileIds
                    .stream()
                    .distinct()
                    .collect(Collectors.toList());
                genePanelDataForQueriedProfiles = genePanelRepository.fetchGenePanelDataInMultipleMolecularProfilesByPatientIds(distinctMolecularProfileIds, null);
            }
        }

        Map<Pair<String, String>, GenePanelData> genePanelDataSet = genePanelDataForQueriedProfiles
            .stream()
            .collect(toMap(
                datum -> new Pair(datum.getMolecularProfileId(),
                    datum.getPatientId()), Function.identity()));

        for (int i = 0; i < molecularProfileIds.size(); i++) {
            String molecularProfileId = molecularProfileIds.get(i);
            String patientId = patientIds.get(i);
            if(molecularProfileId.endsWith("_fusion")) {
                String mutationMolecularProfileId = molecularProfileId.replace("_fusion", "_mutations");
                Pair<String, String> key = new Pair(mutationMolecularProfileId, patientId);
                if(genePanelDataSet.containsKey(key)) {
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
            } else {
                Pair<String, String> key = new Pair(molecularProfileId, patientId);
                if(genePanelDataSet.containsKey(key)) {
                    genePanelData.add(genePanelDataSet.get(key));
                }
            }
        }
        return genePanelData;
    }
}
