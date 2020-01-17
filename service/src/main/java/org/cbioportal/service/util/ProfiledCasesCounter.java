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

        Set<String> casesWithoutPanelData = profiled
                .stream()
                .filter(g -> g.getGenePanelId() == null)
                // there can be duplicate patient or sample id, append study id
                .map(x -> x.getStudyId() + (countByPatients ? x.getPatientId() : x.getSampleId()))
                .collect(Collectors.toSet());

        for (AlterationCountByGene alterationCountByGene : alterationCounts) {
            final Set<String> profiledCasesForGene = new HashSet<String>();
            Integer entrezGeneId = alterationCountByGene.getEntrezGeneId();
            List<GenePanel> allPanels = new ArrayList<>();
            
            if (geneGenePanelMap.containsKey(entrezGeneId)) {
                geneGenePanelMap.get(entrezGeneId).forEach(genePanel -> {
                    Set<String> casesWithPanelData = genePanelDataMap
                            .get(genePanel.getStableId())
                            .stream()
                            // there can be duplicate patient or sample id, append study id
                            .map(x -> x.getStudyId() + (countByPatients ? x.getPatientId() : x.getSampleId()))
                            .collect(Collectors.toSet());
                    profiledCasesForGene.addAll(casesWithPanelData);
                    allPanels.add(genePanel);
                });
                
                profiledCasesForGene.addAll(casesWithoutPanelData);
            } else {
                
                profiledCasesForGene.addAll(profiledCases);
            }
            
            alterationCountByGene.setMatchingGenePanelIds(
                    allPanels.stream().map(panel -> panel.getStableId()).collect(Collectors.toSet()));
            alterationCountByGene.setNumberOfProfiledCases(profiledCasesForGene.size());
        }
    }
}
