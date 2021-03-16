package org.cbioportal.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
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

        boolean hasFusionProfileIdsInQuery = molecularProfileIds
            .stream()
            .anyMatch(molecularProfileId -> molecularProfileId.endsWith("_fusion"));

        if (!hasFusionProfileIdsInQuery) {
            return genePanelRepository.fetchGenePanelDataInMultipleMolecularProfiles(molecularProfileIds, sampleIds);
        }

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

        List<GenePanelData> genePanelData = genePanelRepository
            .fetchGenePanelDataInMultipleMolecularProfiles(updatedMolecularProfileIds, updatedSampleIds);

        Map<Pair<String, String>, GenePanelData> genePanelDataSet = genePanelData
            .stream()
            .collect(toMap(
                    datum -> new Pair(datum.getMolecularProfileId(),
                    datum.getSampleId()), Function.identity()));

        return genePanelData.stream().map(datum -> {
            String molecularProfileId = datum.getMolecularProfileId();
            if(molecularProfileId.endsWith("_fusion")) {
                String mutationProfileIdToSearch = molecularProfileId.replace("_fusion", "_mutations");
                Pair key = new Pair(mutationProfileIdToSearch, datum.getSampleId());
                if(genePanelDataSet.containsKey(key)) {
                    GenePanelData mutationGenePanelData = genePanelDataSet.get(key);
                    datum.setGenePanelId(mutationGenePanelData.getGenePanelId());
                    datum.setProfiled(mutationGenePanelData.getProfiled());
                }
            }
            return datum;
        }).collect(toList());
        // TODO: remove this block after fusion are migrated to structural variant in database
	}

    @Override
    public List<GenePanelData> fetchGenePanelDataInMultipleMolecularProfilesByPatientIds(
            List<String> molecularProfileIds, List<String> patientIds) {

        boolean hasFusionProfileIdsInQuery = molecularProfileIds
            .stream()
            .anyMatch(molecularProfileId -> molecularProfileId.endsWith("_fusion"));

        if (!hasFusionProfileIdsInQuery) {
            return genePanelRepository
                .fetchGenePanelDataInMultipleMolecularProfilesByPatientIds(molecularProfileIds, patientIds);
        }

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

        List<GenePanelData> genePanelData = genePanelRepository
            .fetchGenePanelDataInMultipleMolecularProfiles(updatedMolecularProfileIds, updatedPatientIds);

        Map<Pair<String, String>, GenePanelData> genePanelDataSet = genePanelData
            .stream()
            .collect(toMap(
                datum -> new Pair(datum.getMolecularProfileId(),
                    datum.getSampleId()), Function.identity()));

        return genePanelData.stream().map(datum -> {
            String molecularProfileId = datum.getMolecularProfileId();
            if(molecularProfileId.endsWith("_fusion")) {
                String mutationProfileIdToSearch = molecularProfileId.replace("_fusion", "_mutations");
                Pair key = new Pair(mutationProfileIdToSearch, datum.getSampleId());
                if(genePanelDataSet.containsKey(key)) {
                    GenePanelData mutationGenePanelData = genePanelDataSet.get(key);
                    datum.setGenePanelId(mutationGenePanelData.getGenePanelId());
                    datum.setProfiled(mutationGenePanelData.getProfiled());
                }
            }
            return datum;
        }).collect(toList());
        // TODO: remove this block after fusion are migrated to structural variant in database
    }
}
