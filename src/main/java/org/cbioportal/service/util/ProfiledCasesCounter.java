package org.cbioportal.service.util;

import org.apache.commons.math3.util.Pair;
import org.cbioportal.model.AlterationCountBase;
import org.cbioportal.model.AlterationCountByGene;
import org.cbioportal.model.AlterationCountByStructuralVariant;
import org.cbioportal.model.GenePanel;
import org.cbioportal.model.GenePanelData;
import org.cbioportal.model.GenePanelToGene;
import org.cbioportal.service.GenePanelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class ProfiledCasesCounter<T extends AlterationCountBase> {

    @Autowired
    private GenePanelService genePanelService;

    Function<GenePanelData, String> sampleUniqueIdentifier = sample -> sample.getStudyId() + sample.getSampleId();
    Function<GenePanelData, String> patientUniqueIdentifier = sample -> sample.getStudyId() + sample.getPatientId();

    private enum ProfiledCaseType {
        SAMPLE, PATIENT
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

        Map<Pair<Integer, String>, List<GenePanel>> geneToGenePanel = new HashMap<>();
        for (GenePanel genePanel : genePanels) {
            for (GenePanelToGene genePanelToGene : genePanel.getGenes()) {
                // TODO I am not sure whether it is smart to include the HUGO gene suymbol in the key here.
                // What if two panels have the same Entrez gene id with different Hugo gene symbols? If that situation
                // can never occur, better not include the HUGO symbol in the key and only use entrez gene id. It confuses
                // developers that may think it is an important key element.
                Pair<Integer, String> key = new Pair<>(genePanelToGene.getEntrezGeneId(), genePanelToGene.getHugoGeneSymbol());
                if (geneToGenePanel.containsKey(key)) {
                    geneToGenePanel.get(key).add(genePanel);
                } else {
                    List<GenePanel> geneGenePanelList = new ArrayList<>();
                    geneGenePanelList.add(genePanel);
                    geneToGenePanel.put(key, geneGenePanelList);
                }
            }
        }

        List<GenePanelData> genePanelData = genePanelDataList
            .stream()
            .filter(GenePanelData::getProfiled)
            .collect(Collectors.toList());

        Set<String> profiledCases = genePanelData
            .stream()
            // there can be duplicate patient or sample id, append study id
            .map(caseUniqueIdentifier)
            .collect(Collectors.toSet());
        int profiledCasesCount = profiledCases.size();

        // here we look for cases where none of the profiles have gene panel ids
        // a case with at least one profile with gene panel id is considered as a case with gene panel data
        // so a case is considered without panel data only if none of the profiles has a gene panel id

        // first identify cases with gene panel data
        Set<String> casesWithPanelData = genePanelData
            .stream()
            .filter(g -> g.getGenePanelId() != null)
            // there can be duplicate patient or sample id, append study id
            .map(caseUniqueIdentifier)
            .collect(Collectors.toSet());

        // find all unique cases
        Set<String> casesWithoutPanelData = genePanelData
            .stream()
            // there can be duplicate patient or sample id, append study id
            .map(caseUniqueIdentifier)
            .collect(Collectors.toSet());

        // removing cases with panel data from all unique cases gives us the cases without panel data
        casesWithoutPanelData.removeAll(casesWithPanelData);

        for (T alterationCount : alterationCounts) {
            Set<String> totalProfiledPatients = new HashSet<>();
            Set<String> allMatchingGenePanelIds = new HashSet<>();
            int totalProfiledSamples = 0;
            // different calculations depending on if gene is linked to gene panels
            if (alterationIsCoveredByGenePanel(alterationCount, geneToGenePanel)) {
                // for every gene panel associated containing the gene, use the sum of unique cases
                // as well as cases without panel data
                for (GenePanel genePanel : getGenePanelsForAlterationCount(alterationCount, geneToGenePanel)) {
                    allMatchingGenePanelIds.add(genePanel.getStableId());
                    if (profiledCaseType == ProfiledCaseType.PATIENT) {
                        totalProfiledPatients.addAll(casesWithDataInGenePanel.get(genePanel.getStableId()));
                    } else {
                        totalProfiledSamples += casesWithDataInGenePanel.get(genePanel.getStableId()).size();
                    }
                }
                if (profiledCaseType == ProfiledCaseType.PATIENT) {
                    totalProfiledPatients.addAll(casesWithoutPanelData);
                    alterationCount.setNumberOfProfiledCases(totalProfiledPatients.size());
                } else {
                    totalProfiledSamples += casesWithoutPanelData.size();
                    alterationCount.setNumberOfProfiledCases(totalProfiledSamples);
                }
            } else {
                // we use profiledCasesCount instead of casesWithoutPanelData to
                // prevent a divide by zero error which can happen for targeted studies
                // in which certain genes have events that are not captured by the panel.
                alterationCount.setNumberOfProfiledCases(profiledCasesCount);
            }
            alterationCount.setMatchingGenePanelIds(allMatchingGenePanelIds);
        }

        if (includeMissingAlterationsFromGenePanel) {
            Set<Integer> genesWithAlteration = alterationCounts.stream()
                .flatMap(count -> Arrays.stream(count.getEntrezGeneIds()))
                .collect(Collectors.toSet());

            geneToGenePanel.entrySet().forEach(entry -> {
                Integer entrezGeneId = entry.getKey().getFirst();
                String hugoGeneSymbol = entry.getKey().getSecond();
                // add alterationCount object where there are no alterations but have genePanel
                // object
                if (!genesWithAlteration.contains(entrezGeneId)) {
                    AlterationCountByGene alterationCountByGene = new AlterationCountByGene();

                    Set<String> totalProfiledPatients = new HashSet<>();
                    Set<String> allMatchingGenePanelIds = new HashSet<>();
                    int totalProfiledSamples = 0;
                    for (GenePanel genePanel : geneToGenePanel.get(new Pair<>(entrezGeneId, hugoGeneSymbol))) {
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

        Map<String, Set<String>> casesWithDataInGenePanel = new HashMap<>();
        // loop through all membership records -- ignore any where g.getGenePanelId == null
        for (GenePanelData genePanelDataRecord : genePanelDataList) {
            String associatedGenePanel = genePanelDataRecord.getGenePanelId();
            if (associatedGenePanel != null) {
                casesWithDataInGenePanel.putIfAbsent(associatedGenePanel, new HashSet<>());
                Set<String> casesForThisGenePanel = casesWithDataInGenePanel.get(associatedGenePanel);
                casesForThisGenePanel.add(caseUniqueIdentifier.apply(genePanelDataRecord));
            }
        }
        return casesWithDataInGenePanel;
    }

    private boolean alterationIsCoveredByGenePanel(T alterationCount, Map<Pair<Integer, String>, List<GenePanel>> entrezIdToGenePanel) {
        return !getGenePanelsForAlterationCount(alterationCount, entrezIdToGenePanel).isEmpty();
    }

    private List<GenePanel> getGenePanelsForAlterationCount(T alterationCount, Map<Pair<Integer, String>, List<GenePanel>> entrezIdToGenePanel) {
        if (alterationCount instanceof AlterationCountByGene) {
            Integer entrezId = ((AlterationCountByGene) alterationCount).getEntrezGeneId();
            String hugoSymbol = ((AlterationCountByGene) alterationCount).getHugoGeneSymbol();
            return entrezIdToGenePanel.getOrDefault(new Pair<>(entrezId, hugoSymbol), new ArrayList<>());
        }
        if (alterationCount instanceof AlterationCountByStructuralVariant) {
            Integer gene1EntrezId = ((AlterationCountByStructuralVariant) alterationCount).getGene1EntrezGeneId();
            String gene1HugoSymbol = ((AlterationCountByStructuralVariant) alterationCount).getGene1HugoGeneSymbol();
            Integer gene2EntrezId = ((AlterationCountByStructuralVariant) alterationCount).getGene2EntrezGeneId();
            String gene2HugoSymbol = ((AlterationCountByStructuralVariant) alterationCount).getGene2HugoGeneSymbol();
            List<GenePanel> panels = entrezIdToGenePanel.getOrDefault(new Pair<>(gene1EntrezId, gene2HugoSymbol), new ArrayList<>());
            panels.addAll(entrezIdToGenePanel.getOrDefault(new Pair<>(gene2EntrezId, gene2HugoSymbol), new ArrayList<>()));
            return panels.stream().distinct().collect(Collectors.toList());
        }
        throw new IllegalArgumentException("At present only AlterationCountByGene or AlterationCountByStructuralVariant are " +
            "supported.");
    }
}
