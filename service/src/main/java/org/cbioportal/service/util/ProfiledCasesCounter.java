package org.cbioportal.service.util;

import java.util.*;
import java.util.stream.Collectors;

import org.cbioportal.model.*;
import org.cbioportal.service.GenePanelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ProfiledCasesCounter {

    @Autowired
    private GenePanelService genePanelService;

    public void calculate(List<String> molecularProfileIds, List<String> sampleIds,
            List<? extends AlterationCountByGene> alterationCounts, boolean countByPatients) {

        List<GenePanelData> genePanelDataList = genePanelService
                .fetchGenePanelDataInMultipleMolecularProfiles(molecularProfileIds, sampleIds);
        Map<String, List<GenePanelData>> genePanelDataMap = genePanelDataList.stream()
                .filter(g -> g.getGenePanelId() != null).collect(Collectors.groupingBy(GenePanelData::getGenePanelId));
        List<GenePanel> genePanels = new ArrayList<>();
        if (!genePanelDataMap.isEmpty()) {
            genePanels = genePanelService.fetchGenePanels(new ArrayList<>(genePanelDataMap.keySet()), "DETAILED");
        }

        Map<Integer, List<GenePanel>> geneGenePanelMap = new HashMap<>();
        for (GenePanel genePanel : genePanels) {
            for (GenePanelToGene genePanelToGene : genePanel.getGenes()) {
                Integer entrezGeneId = genePanelToGene.getEntrezGeneId();
                if (geneGenePanelMap.containsKey(entrezGeneId)) {
                    geneGenePanelMap.get(entrezGeneId).add(genePanel);
                } else {
                    List<GenePanel> geneGenePanelList = new ArrayList<>();
                    geneGenePanelList.add(genePanel);
                    geneGenePanelMap.put(entrezGeneId, geneGenePanelList);
                }
            }
        }

        List<GenePanelData> profiled = genePanelDataList
                .stream()
                .filter(GenePanelData::getProfiled)
                .collect(Collectors.toList());

        Set<String> profiledCases = profiled
                .stream()
                // there can be duplicate patient or sample id, append study id
                .map(x -> x.getStudyId() + (countByPatients ? x.getPatientId() : x.getSampleId()))
                .collect(Collectors.toSet());
        int profiledCasesCount = profiledCases.size(); 

        Set<String> casesWithoutPanelData = profiled
                .stream()
                .filter(g -> g.getGenePanelId() == null)
                // there can be duplicate patient or sample id, append study id
                .map(x -> x.getStudyId() + (countByPatients ? x.getPatientId() : x.getSampleId()))
                .collect(Collectors.toSet());

        
        Map<String, Set<String>> genePanelToCases = new HashMap<String, Set<String>>();
        
        for (AlterationCountByGene alterationCountByGene : alterationCounts) {
            final Set<String> profiledCasesForGene = new HashSet<String>();
            Integer entrezGeneId = alterationCountByGene.getEntrezGeneId();
            List<GenePanel> allPanels = new ArrayList<>();
            
            Set<String> totalProfiledCases = new HashSet<String>();
            Set<String> allMatchingGenePanelIds = new HashSet<String>();
            // different calculations depending on if gene is linked to gene panels 
            if (geneGenePanelMap.containsKey(entrezGeneId)) {
                // calculate and store cases for each gene panel
                for (GenePanel genePanel : geneGenePanelMap.get(entrezGeneId)) {
                    if (!genePanelToCases.containsKey(genePanel.getStableId())) {
                        Set<String> casesWithPanelData = genePanelDataMap
                            .get(genePanel.getStableId())
                            .stream()
                            .map(x -> x.getStudyId() + (countByPatients ? x.getPatientId() : x.getSampleId()))
                            .collect(Collectors.toSet());
                        genePanelToCases.put(genePanel.getStableId(), casesWithPanelData);
                    }
                }
                // for every gene panel associated containing the gene, use the sum of unique cases
                // as well as cases without panel data
                for (GenePanel genePanel : geneGenePanelMap.get(entrezGeneId)) {
                    allMatchingGenePanelIds.add(genePanel.getStableId());
                    totalProfiledCases.addAll(genePanelToCases.get(genePanel.getStableId()));
                    totalProfiledCases.addAll(casesWithoutPanelData);
                }
                alterationCountByGene.setNumberOfProfiledCases(totalProfiledCases.size());
                alterationCountByGene.setMatchingGenePanelIds(allMatchingGenePanelIds);
            } else {
                alterationCountByGene.setNumberOfProfiledCases(profiledCasesCount);
                alterationCountByGene.setMatchingGenePanelIds(allMatchingGenePanelIds);
            }
        }
    }
}
