package org.cbioportal.service.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.cbioportal.model.AlterationCountByGene;
import org.cbioportal.model.GenePanel;
import org.cbioportal.model.GenePanelData;
import org.cbioportal.model.GenePanelToGene;
import org.cbioportal.service.GenePanelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GeneFrequencyCalculator {

    @Autowired
    private GenePanelService genePanelService;

    public void calculate(List<String> molecularProfileIds, List<String> sampleIds, 
        List<? extends AlterationCountByGene> alterationCounts) {

        List<GenePanelData> genePanelDataList = genePanelService.fetchGenePanelDataInMultipleMolecularProfiles(molecularProfileIds, sampleIds);
        Map<String, List<GenePanelData>> genePanelDataMap = genePanelDataList.stream().filter(g -> 
            g.getGenePanelId() != null).collect(Collectors.groupingBy(GenePanelData::getGenePanelId));
        List<GenePanel> genePanels = new ArrayList<>();
        if (!genePanelDataMap.isEmpty()) {
            genePanels = genePanelService.fetchGenePanels(new ArrayList<>(genePanelDataMap.keySet()), "DETAILED");
        }

        Map<Integer, List<GenePanel>> geneGenePanelMap = new HashMap<>();
        for (GenePanel genePanel: genePanels) {
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

        for (AlterationCountByGene alterationCountByGene : alterationCounts) {

            int denominator = 0;
            Integer entrezGeneId = alterationCountByGene.getEntrezGeneId();
            List<GenePanelData> profiled = genePanelDataList.stream().filter(g -> g.getProfiled()).collect(Collectors.toList());
            if (geneGenePanelMap.containsKey(entrezGeneId)) {
                List<GenePanel> matchingGenePanels = geneGenePanelMap.get(entrezGeneId);
                for (GenePanel genePanel : matchingGenePanels) {
                    denominator += genePanelDataMap.get(genePanel.getStableId()).size();
                }
                
                denominator += profiled.stream().filter(g -> g.getGenePanelId() == null).count();
            } else {
                denominator = profiled.size();
            }
            alterationCountByGene.setNumberOfSamplesProfiled(denominator);
        }
    }
}
