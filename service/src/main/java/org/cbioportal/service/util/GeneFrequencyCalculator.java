package org.cbioportal.service.util;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.cbioportal.model.AlterationCountByGene;
import org.cbioportal.model.GenePanel;
import org.cbioportal.model.GenePanelData;
import org.cbioportal.model.GenePanelToGene;
import org.cbioportal.model.MolecularProfile;
import org.cbioportal.model.SampleList;
import org.cbioportal.service.GenePanelService;
import org.cbioportal.service.SampleListService;
import org.cbioportal.service.exception.MolecularProfileNotFoundException;
import org.cbioportal.service.exception.SampleListNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GeneFrequencyCalculator {

    @Autowired
    private SampleListService sampleListService;
    @Autowired
    private GenePanelService genePanelService;

    public void calculate(MolecularProfile molecularProfile, List<String> sampleIds, 
        List<? extends AlterationCountByGene> alterationCounts, String listIdSuffix) throws MolecularProfileNotFoundException {

        List<String> finalSampleIds = new ArrayList<>();
        try {
            SampleList sequencedSampleList = sampleListService.getSampleList(
                molecularProfile.getCancerStudyIdentifier() + listIdSuffix);
            Set<String> idsSet = new HashSet<>(sequencedSampleList.getSampleIds());
            idsSet.retainAll(new HashSet<>(sampleIds));
            finalSampleIds.addAll(idsSet);
        } catch (SampleListNotFoundException ex) {
            finalSampleIds = sampleIds;
        }

        List<GenePanelData> genePanelDataList = genePanelService.fetchGenePanelData(molecularProfile.getStableId(), finalSampleIds);
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
            if (geneGenePanelMap.containsKey(entrezGeneId)) {
                List<GenePanel> matchingGenePanels = geneGenePanelMap.get(entrezGeneId);
                for (GenePanel genePanel : matchingGenePanels) {
                    denominator += genePanelDataMap.get(genePanel.getStableId()).size();
                }
                
                denominator += genePanelDataList.stream().filter(g -> g.getProfiled() && g.getGenePanelId() == null).count();
            } else {
                denominator = finalSampleIds.size();
            }
            alterationCountByGene.setFrequency(new BigDecimal((double) alterationCountByGene.getCountByEntity() / 
                denominator * 100).setScale(2, BigDecimal.ROUND_HALF_UP));
        }
    }
}
