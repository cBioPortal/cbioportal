package org.cbioportal.service.util;

import java.util.*;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.cbioportal.model.*;
import org.cbioportal.service.GenePanelService;
import org.cbioportal.service.MolecularProfileService;
import org.cbioportal.service.SampleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ProfiledCasesCounter<T extends AlterationCountByGene> {

    @Autowired
    private GenePanelService genePanelService;
    @Autowired
    private MolecularProfileService molecularProfileService;
    @Autowired
    private SampleService sampleService;

    public void calculate(List<String> molecularProfileIds, List<String> sampleIds,
            List<T> alterationCounts, boolean countByPatients, boolean includeMissingAlterationsFromGenePanel) {
        List<GenePanelData> genePanelDataList = genePanelService
                .fetchGenePanelDataInMultipleMolecularProfiles(molecularProfileIds, sampleIds);
        Map<String, Set<String>> casesWithDataInGenePanel = extractCasesWithDataInGenePanel(genePanelDataList, countByPatients);
        List<GenePanel> genePanels = new ArrayList<>();
        if (!casesWithDataInGenePanel.isEmpty()) {
            genePanels = genePanelService.fetchGenePanels(new ArrayList<>(casesWithDataInGenePanel.keySet()), "DETAILED");
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
                .map(x -> computeUniqueCaseId(x, countByPatients))
                .collect(Collectors.toSet());
        int profiledCasesCount = profiledCases.size();

        Set<String> casesWithoutPanelData = profiled
                .stream()
                .filter(g -> g.getGenePanelId() == null)
                // there can be duplicate patient or sample id, append study id
                .map(x -> computeUniqueCaseId(x, countByPatients))
                .collect(Collectors.toSet());

        for (AlterationCountByGene alterationCountByGene : alterationCounts) {
            Integer entrezGeneId = alterationCountByGene.getEntrezGeneId();
            Set<String> totalProfiledCases = new HashSet<String>();
            Set<String> allMatchingGenePanelIds = new HashSet<String>();
            // different calculations depending on if gene is linked to gene panels
            if (geneGenePanelMap.containsKey(entrezGeneId)) {
                // for every gene panel associated containing the gene, use the sum of unique cases
                // as well as cases without panel data
                for (GenePanel genePanel : geneGenePanelMap.get(entrezGeneId)) {
                    allMatchingGenePanelIds.add(genePanel.getStableId());
                    totalProfiledCases.addAll(casesWithDataInGenePanel.get(genePanel.getStableId()));
                }
                totalProfiledCases.addAll(casesWithoutPanelData);
                alterationCountByGene.setNumberOfProfiledCases(totalProfiledCases.size());
            } else {
                alterationCountByGene.setNumberOfProfiledCases(profiledCasesCount);
            }
            alterationCountByGene.setMatchingGenePanelIds(allMatchingGenePanelIds);
        }

        if (includeMissingAlterationsFromGenePanel) {
            Map<Integer, Boolean> genesWithAlteration = alterationCounts.stream()
                    .collect(Collectors.toMap(AlterationCountByGene::getEntrezGeneId, x -> true));

            geneGenePanelMap.entrySet().forEach(entry -> {
                Integer entrezGeneId = entry.getKey();
                // add alterationCount object where there are no alterations but have genePanel
                // object
                if (!genesWithAlteration.containsKey(entrezGeneId)) {
                    AlterationCountByGene alterationCountByGene = new AlterationCountByGene();

                    Set<String> totalProfiledCases = new HashSet<String>();
                    Set<String> allMatchingGenePanelIds = new HashSet<String>();
                    for (GenePanel genePanel : geneGenePanelMap.get(entrezGeneId)) {
                        allMatchingGenePanelIds.add(genePanel.getStableId());
                        totalProfiledCases.addAll(casesWithDataInGenePanel.get(genePanel.getStableId()));
                    }
                    totalProfiledCases.addAll(casesWithoutPanelData);

                    alterationCountByGene.setEntrezGeneId(entrezGeneId);
                    alterationCountByGene.setMatchingGenePanelIds(allMatchingGenePanelIds);
                    alterationCountByGene.setNumberOfProfiledCases(totalProfiledCases.size());
                    alterationCountByGene.setNumberOfAlteredCases(0);

                    alterationCounts.add((T) alterationCountByGene);
                }
            });

        }

    }

    private Map<String, Set<String>> extractCasesWithDataInGenePanel(List<GenePanelData> genePanelDataList, boolean countByPatients) {
        Map<String, Set<String>> casesWithDataInGenePanel = new HashMap<String, Set<String>>();
        // loop through all membership records -- ignore any where g.getGenePanelId == null
        for (GenePanelData genePanelDataRecord : genePanelDataList) {
            String associatedGenePanel = genePanelDataRecord.getGenePanelId();
            if (associatedGenePanel != null) {
                casesWithDataInGenePanel.putIfAbsent(associatedGenePanel, new HashSet<String>());
                Set<String> casesForThisGenePanel = casesWithDataInGenePanel.get(associatedGenePanel);
                casesForThisGenePanel.add(computeUniqueCaseId(genePanelDataRecord, countByPatients));
            }
        }
        return casesWithDataInGenePanel;
    }

    private String computeUniqueCaseId(GenePanelData genePanelDataRecord, boolean countByPatients) {
        if (countByPatients) {
            return genePanelDataRecord.getStudyId() + genePanelDataRecord.getPatientId();
        } else {
            return genePanelDataRecord.getStudyId() + genePanelDataRecord.getSampleId();
        }
    }

    public String computeUniqueCaseId(String studyId, String caseId) {
        return studyId + caseId;
    }

    public String getUniquePatientId(Sample sample) {
        return computeUniqueCaseId(sample.getCancerStudyIdentifier(), sample.getPatientStableId());
    }

    public Map<String, Long> getProfiledCaseCountsByGroup(
            Map<String, List<MolecularProfileCaseIdentifier>> molecularProfileCaseSets, EnrichmentType enrichmentType) {

        return molecularProfileCaseSets.entrySet().stream().collect(Collectors.toMap(Entry::getKey, entry -> {
            List<String> queriedCaseIds = new ArrayList<>();
            List<String> queriedMolecularProfileIds = new ArrayList<>();
            entry.getValue().forEach(pair -> {
                queriedCaseIds.add(pair.getCaseId());
                queriedMolecularProfileIds.add(pair.getMolecularProfileId());
            });

            List<String> sampleIdsToQuery = new ArrayList<>();
            List<String> molecularProfileIdsToQuery = new ArrayList<>();

            if (EnrichmentType.PATIENT.equals(enrichmentType)) {

                Map<String, MolecularProfile> molecularProfileById = molecularProfileService
                        .getMolecularProfiles(new ArrayList<>(new HashSet<>(queriedMolecularProfileIds)), "SUMMARY")
                        .stream()
                        .collect(Collectors.toMap(MolecularProfile::getStableId, Function.identity()));

                List<String> studyIds = queriedMolecularProfileIds
                        .stream()
                        .map(molecularProfileById::get)
                        .map(MolecularProfile::getCancerStudyIdentifier)
                        .collect(Collectors.toList());

                Map<String, List<Sample>> samplesByPatient = sampleService
                        .getSamplesOfPatientsInMultipleStudies(studyIds, queriedCaseIds, "SUMMARY")
                        .stream()
                        .collect(Collectors.groupingBy(sample -> getUniquePatientId(sample)));

                entry.getValue().forEach(pair -> {
                    String patientId = pair.getCaseId();
                    String molecularProfileId = pair.getMolecularProfileId();
                    String studyId = molecularProfileById.get(molecularProfileId).getCancerStudyIdentifier();

                    samplesByPatient
                            .getOrDefault(computeUniqueCaseId(studyId, patientId), new ArrayList<>())
                            .forEach(sample -> {
                                sampleIdsToQuery.add(sample.getStableId());
                                molecularProfileIdsToQuery.add(molecularProfileId);
                            });
                });
            } else {
                sampleIdsToQuery.addAll(queriedCaseIds);
                molecularProfileIdsToQuery.addAll(queriedMolecularProfileIds);
            }

            return genePanelService
                    .fetchGenePanelDataInMultipleMolecularProfiles(molecularProfileIdsToQuery, sampleIdsToQuery)
                    .stream()
                    .map(genePanelData -> computeUniqueCaseId(genePanelData,
                            EnrichmentType.PATIENT.equals(enrichmentType)))
                    .distinct()
                    .count();
        }));
    }
}
