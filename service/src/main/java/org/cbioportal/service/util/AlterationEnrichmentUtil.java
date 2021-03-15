package org.cbioportal.service.util;

import org.apache.commons.math3.stat.inference.ChiSquareTest;
import org.apache.commons.math3.util.Pair;
import org.cbioportal.model.AlterationCountByGene;
import org.cbioportal.model.AlterationEnrichment;
import org.cbioportal.model.CountSummary;
import org.cbioportal.model.EnrichmentType;
import org.cbioportal.model.Gene;
import org.cbioportal.model.GenePanelData;
import org.cbioportal.model.MolecularProfileCaseIdentifier;
import org.cbioportal.service.GenePanelService;
import org.cbioportal.service.GeneService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.Map.Entry;

@Component
public class AlterationEnrichmentUtil<T extends AlterationCountByGene> {

    @Autowired
    private FisherExactTestCalculator fisherExactTestCalculator;
    @Autowired
    private GeneService geneService;
    @Autowired
    private ProfiledCasesCounter<T> profiledCasesCounter;
    @Autowired
    private GenePanelService genePanelService;

    public List<AlterationEnrichment> createAlterationEnrichments(
            Map<String, Pair<List<T>, Long>> mutationCountsbyGroup,
            Map<String, List<MolecularProfileCaseIdentifier>> molecularProfileCaseSets,
            EnrichmentType enrichmentType) {
        
        Map<String, Map<Integer, AlterationCountByGene>> mutationCountsbyEntrezGeneIdAndGroup = mutationCountsbyGroup
                    .entrySet()
                    .stream()
                    .collect(Collectors.toMap(
                            entry -> entry.getKey(),
                            entry -> {
                                //convert list of alterations to map with EntrezGeneId as key
                                return entry.getValue().getFirst().stream()
                                        .collect(Collectors.toMap(AlterationCountByGene::getEntrezGeneId, c -> c));
                            }));

        Map<String, Long> profiledCaseCountsByGroup =  mutationCountsbyGroup
            .entrySet()
            .stream()
            .collect(Collectors.toMap(
                Entry::getKey,
                entry -> entry.getValue().getSecond()));

        Set<Integer> allGeneIds = mutationCountsbyEntrezGeneIdAndGroup
                .values()
                .stream()
                .flatMap(x -> x.keySet().stream())
                .collect(Collectors.toSet());
        
        Set<String> groups = mutationCountsbyEntrezGeneIdAndGroup.keySet();

        List<Gene> genes = geneService.fetchGenes(
                allGeneIds
                    .stream()
                    .map(Object::toString)
                    .collect(Collectors.toList()),
                "ENTREZ_GENE_ID",
                "SUMMARY");

        return genes
                .stream()
                .filter(gene -> {
                    // filter genes where number of altered cases in all groups is 0
                    return groups.stream().filter(group -> {
                        AlterationCountByGene mutationCountByGene = mutationCountsbyEntrezGeneIdAndGroup
                                .getOrDefault(group, new HashMap<Integer, AlterationCountByGene>())
                                .get(gene.getEntrezGeneId());
                        return mutationCountByGene == null ? false : mutationCountByGene.getNumberOfAlteredCases() != 0;
                    }).count() > 0;
                })
                .map(gene -> {
                    AlterationEnrichment alterationEnrichment = new AlterationEnrichment();
                    alterationEnrichment.setEntrezGeneId(gene.getEntrezGeneId());
                    alterationEnrichment.setHugoGeneSymbol(gene.getHugoGeneSymbol());
                    List<CountSummary> counts = groups.stream().map(group -> {
                        CountSummary groupCasesCount = new CountSummary();
                        AlterationCountByGene mutationCountByGene = mutationCountsbyEntrezGeneIdAndGroup
                                .getOrDefault(group, new HashMap<Integer, AlterationCountByGene>())
                                .get(gene.getEntrezGeneId());

                        Integer alteredCount = mutationCountByGene != null ? mutationCountByGene.getNumberOfAlteredCases() : 0;
                        Integer profiledCount = mutationCountByGene != null ? mutationCountByGene.getNumberOfProfiledCases() : profiledCaseCountsByGroup.get(group).intValue();
                        groupCasesCount.setName(group);
                        groupCasesCount.setAlteredCount(alteredCount);
                        groupCasesCount.setProfiledCount(profiledCount);
                        return groupCasesCount;
                    }).collect(Collectors.toList());

                    List<CountSummary> filteredCounts = counts.stream()
                            .filter(groupCasesCount -> groupCasesCount.getProfiledCount() > 0)
                            .collect(Collectors.toList());

                    // groups where number of altered cases is greater than profiled cases.
                    // This is a temporary fix for https://github.com/cBioPortal/cbioportal/issues/7274
                    // and https://github.com/cBioPortal/cbioportal/issues/7418
                    long invalidDataGroups = filteredCounts
                            .stream()
                            .filter( groupCasesCount -> groupCasesCount.getAlteredCount() > groupCasesCount.getProfiledCount())
                            .count();

                    // calculate p-value only if more than one group have profile cases count
                    // greater than 0
                    if (filteredCounts.size() > 1 && invalidDataGroups == 0) {
                        double pValue;
                        // if groups size is two do Fisher Exact test else do Chi-Square test
                        if (groups.size() == 2) {

                            int alteredInNoneCount = counts.get(1).getProfiledCount() - counts.get(1).getAlteredCount();
                            int alteredOnlyInQueryGenesCount = counts.get(0).getProfiledCount()
                                    - counts.get(0).getAlteredCount();

                            pValue = fisherExactTestCalculator.getCumulativePValue(alteredInNoneCount,
                                    counts.get(1).getAlteredCount(), alteredOnlyInQueryGenesCount,
                                    counts.get(0).getAlteredCount());
                        } else {

                            long[][] array = counts.stream().map(count -> {
                                return new long[] { count.getAlteredCount(),
                                        count.getProfiledCount() - count.getAlteredCount() };
                            }).toArray(long[][]::new);

                            ChiSquareTest chiSquareTest = new ChiSquareTest();
                            pValue = chiSquareTest.chiSquareTest(array);

                            // set p-value to 1 when the cases in all groups are altered
                            if (Double.isNaN(pValue)) {
                                pValue = 1;
                            }
                        }
                        alterationEnrichment.setpValue(BigDecimal.valueOf(pValue));
                    }

                    alterationEnrichment.setCounts(counts);
                    return alterationEnrichment;
                }).collect(Collectors.toList());

    }

    public long includeFrequencyForSamples(
            List<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers,
            List<T> alterationCountByGenes,
            boolean includeMissingAlterationsFromGenePanel) {

        // Collect profile id and sample id arrays.
        // These are arrays of equal length, where every index
        // represents a sample id / profile id-combination
        List<String> sampleIds = new ArrayList<>();
        List<String> molecularProfileIds = new ArrayList<>();
        molecularProfileCaseIdentifiers.forEach(pair -> {
            sampleIds.add(pair.getCaseId());
            molecularProfileIds.add(pair.getMolecularProfileId());
        });

        List<GenePanelData> genePanelDataList = genePanelService
                .fetchGenePanelDataInMultipleMolecularProfiles(molecularProfileIds, sampleIds);

        profiledCasesCounter.calculate(alterationCountByGenes, genePanelDataList,
                includeMissingAlterationsFromGenePanel, profiledCasesCounter.sampleUniqueIdentifier);

        return genePanelDataList
            .stream()
            .filter(GenePanelData::getProfiled)
            .map(profiledCasesCounter.sampleUniqueIdentifier)
            .distinct()
            .count();
    }

    public long includeFrequencyForPatients(
            List<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers,
            List<T> alterationCountByGenes,
            boolean includeMissingAlterationsFromGenePanel) {

        // Collect profile id and sample id arrays.
        // These are arrays of equal length, where every index
        // represents a sample id / profile id-combination
        List<String> patientIds = new ArrayList<>();
        List<String> molecularProfileIds = new ArrayList<>();
        molecularProfileCaseIdentifiers.forEach(pair -> {
            patientIds.add(pair.getCaseId());
            molecularProfileIds.add(pair.getMolecularProfileId());
        });

        List<GenePanelData> genePanelDataList = genePanelService
                .fetchGenePanelDataInMultipleMolecularProfilesByPatientIds(molecularProfileIds, patientIds);

        profiledCasesCounter.calculate(alterationCountByGenes, genePanelDataList,
                includeMissingAlterationsFromGenePanel, profiledCasesCounter.patientUniqueIdentifier);

        return genePanelDataList
            .stream()
            .filter(GenePanelData::getProfiled)
            .map(profiledCasesCounter.patientUniqueIdentifier)
            .distinct()
            .count();

    }

}
