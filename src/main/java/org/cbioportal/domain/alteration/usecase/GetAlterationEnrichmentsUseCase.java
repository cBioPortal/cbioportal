package org.cbioportal.domain.alteration.usecase;

import org.apache.commons.math3.stat.inference.ChiSquareTest;
import org.cbioportal.domain.alteration.repository.AlterationRepository;
import org.cbioportal.legacy.model.AlterationCountByGene;
import org.cbioportal.legacy.model.AlterationEnrichment;
import org.cbioportal.legacy.model.CountSummary;
import org.cbioportal.legacy.model.MolecularProfile;
import org.cbioportal.legacy.model.MolecularProfileCaseIdentifier;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.cbioportal.legacy.service.util.FisherExactTestCalculator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.data.util.Pair;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

@Service
@Profile("clickhouse")
public class GetAlterationEnrichmentsUseCase {

    private static final Logger log = LoggerFactory.getLogger(GetAlterationEnrichmentsUseCase.class);

    private Map<String, MolecularProfile> molecularProfilesMap;

    private final AlterationRepository alterationRepository;
    private final ThreadPoolTaskExecutor threadPoolTaskExecutor;

    public GetAlterationEnrichmentsUseCase(AlterationRepository alterationRepository, ThreadPoolTaskExecutor threadPoolTaskExecutor) {
        this.alterationRepository = alterationRepository;
        this.threadPoolTaskExecutor = threadPoolTaskExecutor;
    }

    public Collection<AlterationEnrichment> execute(Map<String, List<MolecularProfileCaseIdentifier>> molecularProfileCaseIdentifierByGroup) {


        Map<String, AlterationEnrichment> alterationEnrichmentByGene = new HashMap<>();

        List<CompletableFuture<Pair<String,List<AlterationCountByGene>>>> futures = molecularProfileCaseIdentifierByGroup
                .entrySet()
                .stream()
                .map((entry) -> CompletableFuture.supplyAsync(() -> this.fetchAlterationCountByGeneByGroup(entry.getKey(), entry.getValue()), threadPoolTaskExecutor))
                .toList();


        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();


        futures.stream().map(CompletableFuture::join).forEach(alterationCountByGeneAndGroup -> {
            var alterationCountByGenes = alterationCountByGeneAndGroup.getSecond();
            var group = alterationCountByGeneAndGroup.getFirst();

            alterationCountByGenes.forEach(alterationCountByGene -> {
                AlterationEnrichment alterationEnrichment =
                        getOrCreateAlterationEnrichment(alterationEnrichmentByGene, alterationCountByGene);

                var countSummary = new CountSummary();
                countSummary.setName(group);
                countSummary.setAlteredCount(alterationCountByGene.getNumberOfAlteredCases());
                countSummary.setProfiledCount(alterationCountByGene.getNumberOfProfiledCases());
                alterationEnrichment.getCounts().add(countSummary);
            });
        });

        // TODO: Check if group counts match and if not insert group with 0 altered and 0 profiled

        var groups = molecularProfileCaseIdentifierByGroup.keySet();

        Collection<AlterationEnrichment> alterationEnrichments = alterationEnrichmentByGene.values().stream()
                .map(alterationEnrichment -> {
                   var pValue = calculateEnrichmentScore(alterationEnrichment);
                   alterationEnrichment.setpValue(pValue);
                   addMissingCountsToAlterationEnrichment(alterationEnrichment, groups);
                   return alterationEnrichment;
                }).collect(Collectors.toSet());
        return alterationEnrichments;
    }

    private Pair<String, List<AlterationCountByGene>> fetchAlterationCountByGeneByGroup(String group,
                                                                                        List<MolecularProfileCaseIdentifier>  molecularProfileCaseIdentifiers){
        Pair<Set<String>, Set<String>> caseIdsAndMolecularProfileIds =
                this.extractCaseIdsAndMolecularProfiles(molecularProfileCaseIdentifiers);

        List<AlterationCountByGene> alterationCountByGenes =
                alterationRepository.getAlterationCountByGeneGivenSamplesAndMolecularProfiles(caseIdsAndMolecularProfileIds.getFirst(),
                        caseIdsAndMolecularProfileIds.getSecond());
        return Pair.of(group, alterationCountByGenes);
    }

    private Pair<Set<String>, Set<String>> extractCaseIdsAndMolecularProfiles(List<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers) {
        Set<String> caseIds = new HashSet<>();
        Set<String> molecularProfileIds = new HashSet<>();
        for (var molecularProfileIdentifier : molecularProfileCaseIdentifiers) {
            String studyId = getStudyIdGivenMolecularProfileId(molecularProfileIdentifier.getMolecularProfileId());
            caseIds.add(studyId+"_"+molecularProfileIdentifier.getCaseId());
            molecularProfileIds.add(molecularProfileIdentifier.getMolecularProfileId());
        }
        return Pair.of(caseIds, molecularProfileIds);
    }


    private AlterationEnrichment getOrCreateAlterationEnrichment(
            Map<String, AlterationEnrichment> alterationEnrichmentByGene, AlterationCountByGene alterationCountByGene) {

        return alterationEnrichmentByGene.computeIfAbsent(alterationCountByGene.getHugoGeneSymbol(), key -> {
            AlterationEnrichment enrichment = new AlterationEnrichment();
            enrichment.setEntrezGeneId(alterationCountByGene.getEntrezGeneId());
            enrichment.setHugoGeneSymbol(alterationCountByGene.getHugoGeneSymbol());
            enrichment.setCounts(new ArrayList<>());
            return enrichment;
        });
    }

    private void addMissingCountsToAlterationEnrichment(AlterationEnrichment alterationEnrichment,
                                                   Collection<String> groups){
        Set<String> counts = alterationEnrichment.getCounts()
                .stream()
                .map(summary -> summary.getName())
                .collect(Collectors.toSet());
        if (counts.size() == groups.size()) {
            return;
        }
        Set<String> groupsWithMissingCounts = new HashSet<>(groups);
        groupsWithMissingCounts.removeAll(counts);

        for (String group : groupsWithMissingCounts) {
            CountSummary countSummary = new CountSummary();
            countSummary.setName(group);
            countSummary.setAlteredCount(0);
            countSummary.setProfiledCount(0);
            alterationEnrichment.getCounts().add(countSummary);
        }

    }

    private BigDecimal calculateEnrichmentScore(AlterationEnrichment alterationEnrichment) {
        double pValue = 0;
        // groups where number of altered cases is greater than profiled cases.
        // This is a temporary fix for https://github.com/cBioPortal/cbioportal/issues/7274
        // and https://github.com/cBioPortal/cbioportal/issues/7418
        long invalidDataGroups = alterationEnrichment.getCounts()
                .stream()
                .filter(groupCasesCount -> groupCasesCount.getAlteredCount() > groupCasesCount.getProfiledCount())
                .count();

        // calculate p-value only if more than one group have profile cases count
        // greater than 0
        if (alterationEnrichment.getCounts().size() > 1 && invalidDataGroups == 0) {
            // if groups size is two do Fisher Exact test else do Chi-Square test
            if (alterationEnrichment.getCounts().size() == 2) {

                int alteredInNoneCount =
                        alterationEnrichment.getCounts().get(1).getProfiledCount() - alterationEnrichment.getCounts().get(1).getAlteredCount();
                int alteredOnlyInQueryGenesCount = alterationEnrichment.getCounts().get(0).getProfiledCount()
                        - alterationEnrichment.getCounts().get(0).getAlteredCount();

                var fisherExactTestCalculator = new FisherExactTestCalculator();
                pValue = fisherExactTestCalculator.getTwoTailedPValue(alteredInNoneCount,
                        alterationEnrichment.getCounts().get(1).getAlteredCount(), alteredOnlyInQueryGenesCount,
                        alterationEnrichment.getCounts().get(0).getAlteredCount());
            } else {

                long[][] array = alterationEnrichment.getCounts().stream().map(count -> {
                    return new long[]{count.getAlteredCount(),
                            count.getProfiledCount() - count.getAlteredCount()};
                }).toArray(long[][]::new);

                ChiSquareTest chiSquareTest = new ChiSquareTest();
                pValue = chiSquareTest.chiSquareTest(array);

                // set p-value to 1 when the cases in all groups are altered
                if (Double.isNaN(pValue)) {
                    pValue = 1;
                }
            }
        }
        return BigDecimal.valueOf(pValue);
    }

    private String getStudyIdGivenMolecularProfileId(String molecularProfileId) {
        if (molecularProfilesMap == null) {
            molecularProfilesMap = alterationRepository.getAllMolecularProfiles().stream().collect(
                    Collectors.toMap(MolecularProfile::getStableId, molecularProfile -> molecularProfile));
        }
        var molecularProfile = molecularProfilesMap.get(molecularProfileId);
        if (molecularProfile == null) {
            //
        }
        return molecularProfile.getCancerStudyIdentifier();

    }
}

