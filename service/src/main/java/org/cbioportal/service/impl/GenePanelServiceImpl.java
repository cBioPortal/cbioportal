package org.cbioportal.service.impl;

import org.cbioportal.model.Gene;
import org.cbioportal.model.GenePanel;
import org.cbioportal.model.GenePanelData;
import org.cbioportal.model.GenePanelToGene;
import org.cbioportal.model.MolecularProfile;
import org.cbioportal.model.Sample;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.persistence.GenePanelRepository;
import org.cbioportal.persistence.SampleListRepository;
import org.cbioportal.service.GenePanelService;
import org.cbioportal.service.GeneService;
import org.cbioportal.service.MolecularProfileService;
import org.cbioportal.service.SampleService;
import org.cbioportal.service.exception.GenePanelNotFoundException;
import org.cbioportal.service.exception.MolecularProfileNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class GenePanelServiceImpl implements GenePanelService {
    
    @Autowired
    private GenePanelRepository genePanelRepository;
    @Autowired
    private MolecularProfileService molecularProfileService;
    @Autowired
    private SampleListRepository sampleListRepository;
    @Autowired
    private SampleService sampleService;
    @Autowired
    private GeneService geneService;

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
    public List<GenePanelData> getGenePanelData(String molecularProfileId, String sampleListId, 
                                                List<Integer> entrezGeneIds) throws MolecularProfileNotFoundException {

        molecularProfileService.getMolecularProfile(molecularProfileId);
        List<String> sampleIds = sampleListRepository.getAllSampleIdsInSampleList(sampleListId);
        List<String> molecularProfileIds = new ArrayList<>();
        sampleIds.forEach(s -> molecularProfileIds.add(molecularProfileId));
        
        return createGenePanelData(genePanelRepository.getGenePanelData(molecularProfileId, sampleListId), 
            molecularProfileIds, sampleIds, entrezGeneIds);
    }

    @Override
    public List<GenePanelData> fetchGenePanelData(String molecularProfileId, List<String> sampleIds, 
                                                  List<Integer> entrezGeneIds) throws MolecularProfileNotFoundException {

        molecularProfileService.getMolecularProfile(molecularProfileId);
        List<String> molecularProfileIds = new ArrayList<>();
        sampleIds.forEach(s -> molecularProfileIds.add(molecularProfileId));

        return createGenePanelData(genePanelRepository.fetchGenePanelData(molecularProfileId, sampleIds), 
            molecularProfileIds, sampleIds, entrezGeneIds);
    }

    @Override
	public List<GenePanelData> fetchGenePanelDataInMultipleMolecularProfiles(List<String> molecularProfileIds,
			List<String> sampleIds, List<Integer> entrezGeneIds) {
        
        return createGenePanelData(genePanelRepository.fetchGenePanelDataInMultipleMolecularProfiles(
            molecularProfileIds, sampleIds), molecularProfileIds, sampleIds, entrezGeneIds);
	}

    private List<GenePanelData> createGenePanelData(List<GenePanelData> genePanelDataList, List<String> molecularProfileIds,
        List<String> sampleIds, List<Integer> entrezGeneIds) {

        List<Gene> genes = geneService.fetchGenes(entrezGeneIds.stream().map(e -> String.valueOf(e)).collect(Collectors.toList()), 
            "ENTREZ_GENE_ID", "ID");

        Map<String, MolecularProfile> molecularProfileMap = molecularProfileService.getMolecularProfiles(
            molecularProfileIds, "SUMMARY").stream().collect(Collectors.toMap(MolecularProfile::getStableId, Function.identity()));
        List<String> studyIds = new ArrayList<>();
        List<String> copyMolecularProfileIds = new ArrayList<>(molecularProfileIds);
        
        for (int i = 0; i < copyMolecularProfileIds.size(); i++) {
            String molecularProfileId = copyMolecularProfileIds.get(i);
            if (molecularProfileMap.containsKey(molecularProfileId)) {
                studyIds.add(molecularProfileMap.get(molecularProfileId).getCancerStudyIdentifier());
            } else {
                molecularProfileIds.remove(i);
                sampleIds.remove(i);
            }
        }

        List<Sample> samples = sampleService.fetchSamples(studyIds, sampleIds, "ID");

        List<GenePanelToGene> genePanelToGeneList = genePanelRepository.getGenesOfPanels(genePanelDataList.stream()
            .map(GenePanelData::getGenePanelId).collect(Collectors.toList()));

        List<GenePanelData> resultGenePanelDataList = new ArrayList<>();

        for(int i = 0; i < sampleIds.size(); i++) {
            String sampleId = sampleIds.get(i);
            String molecularProfileId = molecularProfileIds.get(i);
            for (Gene gene : genes) {
                Integer entrezGeneId = gene.getEntrezGeneId();
                GenePanelData resultGenePanelData = new GenePanelData();
                Optional<GenePanelData> genePanelData = genePanelDataList.stream().filter(g -> g.getMolecularProfileId().equals(
                    molecularProfileId) && g.getSampleId().equals(sampleId)).findFirst();
                if (genePanelData.isPresent()) {
                    String genePanelId = genePanelData.get().getGenePanelId();
                    resultGenePanelData.setGenePanelId(genePanelId);
                    resultGenePanelData.setSequenced(genePanelToGeneList.stream().anyMatch(g -> 
                        g.getGenePanelId().equals(genePanelId) && g.getEntrezGeneId().equals(entrezGeneId)));
                    resultGenePanelData.setWholeExomeSequenced(false);
                } else {
                    resultGenePanelData.setWholeExomeSequenced(true);
                    resultGenePanelData.setSequenced(true);
                }
                resultGenePanelData.setEntrezGeneId(entrezGeneId);
                resultGenePanelData.setMolecularProfileId(molecularProfileId);
                resultGenePanelData.setSampleId(sampleId);
                String studyId = molecularProfileMap.get(molecularProfileId).getCancerStudyIdentifier();
                resultGenePanelData.setStudyId(studyId);
                Optional<Sample> sample = samples.stream().filter(s -> s.getStableId().equals(sampleId) && 
                    s.getCancerStudyIdentifier().equals(studyId)).findFirst();
                if (sample.isPresent()) {
                    resultGenePanelData.setPatientId(sample.get().getPatientStableId());
                    resultGenePanelDataList.add(resultGenePanelData);
                }
            }
        }

        return resultGenePanelDataList;
    }
}
