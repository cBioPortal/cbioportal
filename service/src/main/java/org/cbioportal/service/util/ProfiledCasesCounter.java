package org.cbioportal.service.util;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.math3.util.Pair;
import org.cbioportal.model.*;
import org.cbioportal.service.GenePanelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ProfiledCasesCounter<T extends AlterationCountByGene> {

    @Autowired
    private GenePanelService genePanelService;

    Function<GenePanelData, String> sampleUniqueIdentifier = sample -> sample.getStudyId() + sample.getSampleId();
    Function<GenePanelData, String> patientUniqueIdentifier = sample -> sample.getStudyId() + sample.getPatientId();

    private enum ProfiledCaseType {
        SAMPLE, PATIENT;
    }

    public void calculate(List<T> alterationCounts,
            List<GenePanelData> genePanelDataList,
            boolean includeMissingAlterationsFromGenePanel,
            Function<GenePanelData, String> caseUniqueIdentifier) {
        ProfiledCaseType profiledCaseType = (caseUniqueIdentifier == patientUniqueIdentifier) ?
            ProfiledCaseType.PATIENT : ProfiledCaseType.SAMPLE;
        Map<String, Set<String>> casesWithDataInGenePanel = extractCasesWithDataInGenePanel(genePanelDataList, caseUniqueIdentifier);
        List<GenePanel> genePanels = new ArrayList<>();
        if (!casesWithDataInGenePanel.isEmpty()) {
            genePanels = genePanelService.fetchGenePanels(new ArrayList<>(casesWithDataInGenePanel.keySet()), "DETAILED");
        }

        Map<Pair<Integer, String>, List<GenePanel>> geneGenePanelMap = new HashMap<>();
        for (GenePanel genePanel : genePanels) {
            for (GenePanelToGene genePanelToGene : genePanel.getGenes()) {
                // TODO here we need to adapt for structural variants. 
                Pair<Integer, String> key = new Pair<>(genePanelToGene.getEntrezGeneId(), genePanelToGene.getHugoGeneSymbol());
                if (geneGenePanelMap.containsKey(key)) {
                    geneGenePanelMap.get(key).add(genePanel);
                } else {
                    List<GenePanel> geneGenePanelList = new ArrayList<>();
                    geneGenePanelList.add(genePanel);
                    geneGenePanelMap.put(key, geneGenePanelList);
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
                .map(caseUniqueIdentifier)
                .collect(Collectors.toSet());
        int profiledCasesCount = profiledCases.size();
        
        // here we look for cases where none of the profiles have gene panel ids
        // a case with at least one profile with gene panel id is considered as a case with gene panel data
        // so a case is considered without panel data only if none of the profiles has a gene panel id
        
        // first identify cases with gene panel data
        Set<String> casesWithPanelData = profiled
                .stream()
                .filter(g -> g.getGenePanelId() != null)
                // there can be duplicate patient or sample id, append study id
                .map(caseUniqueIdentifier)
                .collect(Collectors.toSet());

        // find all unique cases
        Set<String> casesWithoutPanelData = profiled
            .stream()
            // there can be duplicate patient or sample id, append study id
            .map(caseUniqueIdentifier)
            .collect(Collectors.toSet());

        // removing cases with panel data from all unique cases gives us the cases without panel data
        casesWithoutPanelData.removeAll(casesWithPanelData);
        
        for (AlterationCountByGene alterationCountByGene : alterationCounts) {
            Integer entrezGeneId = alterationCountByGene.getEntrezGeneId();
            Set<String> totalProfiledPatients = new HashSet<String>();
            int totalProfiledSamples = 0;
            Set<String> allMatchingGenePanelIds = new HashSet<String>();
            Pair<Integer, String> key = new Pair<>(entrezGeneId,alterationCountByGene.getHugoGeneSymbol());
            // different calculations depending on if gene is linked to gene panels
            if (geneGenePanelMap.containsKey(key)) {
                // for every gene panel associated containing the gene, use the sum of unique cases
                // as well as cases without panel data
                for (GenePanel genePanel : geneGenePanelMap.get(key)) {
                    allMatchingGenePanelIds.add(genePanel.getStableId());
                    if (profiledCaseType == ProfiledCaseType.PATIENT) {
                        totalProfiledPatients.addAll(casesWithDataInGenePanel.get(genePanel.getStableId()));
                    } else {
                        totalProfiledSamples += casesWithDataInGenePanel.get(genePanel.getStableId()).size();
                    }
                }
                if (profiledCaseType == ProfiledCaseType.PATIENT) {
                    totalProfiledPatients.addAll(casesWithoutPanelData);
                    alterationCountByGene.setNumberOfProfiledCases(totalProfiledPatients.size());
                } else {
                    totalProfiledSamples += casesWithoutPanelData.size();
                    alterationCountByGene.setNumberOfProfiledCases(totalProfiledSamples);
                }
            } else {
                // we use profiledCasesCount instead of casesWithoutPanelData to
                // prevent a divide by zero error which can happen for targeted studies
                // in which certain genes have events that are not captured by the panel.
                alterationCountByGene.setNumberOfProfiledCases(profiledCasesCount);
            }
            alterationCountByGene.setMatchingGenePanelIds(allMatchingGenePanelIds);
        }

        if (includeMissingAlterationsFromGenePanel) {
            Map<Integer, Boolean> genesWithAlteration = alterationCounts.stream()
                    .collect(Collectors.toMap(AlterationCountByGene::getEntrezGeneId, x -> true));

            geneGenePanelMap.entrySet().forEach(entry -> {
                Pair<Integer, String> key = entry.getKey();
                Integer entrezGeneId = key.getFirst();
                String hugoGeneSymbol = key.getSecond();
                // add alterationCount object where there are no alterations but have genePanel
                // object
                if (!genesWithAlteration.containsKey(entrezGeneId)) {
                    AlterationCountByGene alterationCountByGene = new AlterationCountByGene();

                    Set<String> totalProfiledPatients = new HashSet<String>();
                    int totalProfiledSamples = 0;
                    Set<String> allMatchingGenePanelIds = new HashSet<String>();
                    for (GenePanel genePanel : geneGenePanelMap.get(key)) {
                        allMatchingGenePanelIds.add(genePanel.getStableId());
                        if (profiledCaseType == ProfiledCaseType.PATIENT) {
                            totalProfiledPatients.addAll(casesWithDataInGenePanel.get(genePanel.getStableId()));
                        } else {
                            totalProfiledSamples += casesWithDataInGenePanel.get(genePanel.getStableId()).size();
                        }
                    }
                    if (profiledCaseType == ProfiledCaseType.PATIENT) {
                        totalProfiledPatients.addAll(casesWithoutPanelData);
                    } else {
                        totalProfiledSamples += casesWithoutPanelData.size();
                    }

                    alterationCountByGene.setEntrezGeneId(entrezGeneId);
                    alterationCountByGene.setMatchingGenePanelIds(allMatchingGenePanelIds);
                    if (profiledCaseType == ProfiledCaseType.PATIENT) {
                        alterationCountByGene.setNumberOfProfiledCases(totalProfiledPatients.size());
                    } else {
                        alterationCountByGene.setNumberOfProfiledCases(totalProfiledSamples);
                    }
                    alterationCountByGene.setNumberOfAlteredCases(0);
                    alterationCountByGene.setTotalCount(0);
                    alterationCountByGene.setHugoGeneSymbol(hugoGeneSymbol);

                    alterationCounts.add((T) alterationCountByGene);
                }
            });
        }
    }

    private Map<String, Set<String>> extractCasesWithDataInGenePanel(
            List<GenePanelData> genePanelDataList,
            Function<GenePanelData, String> caseUniqueIdentifier) {

        Map<String, Set<String>> casesWithDataInGenePanel = new HashMap<String, Set<String>>();
        // loop through all membership records -- ignore any where g.getGenePanelId == null
        for (GenePanelData genePanelDataRecord : genePanelDataList) {
            String associatedGenePanel = genePanelDataRecord.getGenePanelId();
            if (associatedGenePanel != null) {
                casesWithDataInGenePanel.putIfAbsent(associatedGenePanel, new HashSet<String>());
                Set<String> casesForThisGenePanel = casesWithDataInGenePanel.get(associatedGenePanel);
                casesForThisGenePanel.add(caseUniqueIdentifier.apply(genePanelDataRecord));
            }
        }
        return casesWithDataInGenePanel;
    }
}
