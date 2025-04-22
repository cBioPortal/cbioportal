package org.cbioportal.domain.alteration.usecase;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import org.cbioportal.domain.alteration.repository.AlterationRepository;
import org.cbioportal.domain.alteration.util.AlterationEnrichmentScoreUtil;
import org.cbioportal.legacy.model.AlterationCountByGene;
import org.cbioportal.legacy.model.AlterationEnrichment;
import org.cbioportal.legacy.model.AlterationFilter;
import org.cbioportal.legacy.model.CountSummary;
import org.cbioportal.legacy.model.EnrichmentType;
import org.cbioportal.legacy.model.MolecularProfile;
import org.cbioportal.legacy.model.MolecularProfileCaseIdentifier;
import org.cbioportal.legacy.service.exception.MolecularProfileNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

@Service
@Profile("clickhouse")
public class GetAlterationEnrichmentsUseCase {

    private static final Logger log = LoggerFactory.getLogger(
        GetAlterationEnrichmentsUseCase.class
    );

    private Map<String, MolecularProfile> molecularProfilesMap;

    private final AlterationRepository alterationRepository;
    private final AsyncTaskExecutor threadPoolTaskExecutor;

    public GetAlterationEnrichmentsUseCase(
        AlterationRepository alterationRepository,
        AsyncTaskExecutor threadPoolTaskExecutor
    ) {
        this.alterationRepository = alterationRepository;
        this.threadPoolTaskExecutor = threadPoolTaskExecutor;
    }

    public Collection<AlterationEnrichment> execute(
        Map<String, List<MolecularProfileCaseIdentifier>> molecularProfileCaseIdentifierByGroup,
        EnrichmentType enrichmentType,
        AlterationFilter alterationFilter
    ) throws MolecularProfileNotFoundException {
        Map<String, AlterationEnrichment> alterationEnrichmentByGene = new HashMap<>();

        List<Pair<String, List<AlterationCountByGene>>> results =
            molecularProfileCaseIdentifierByGroup
                .entrySet()
                .stream()
                .map(entry ->
                    threadPoolTaskExecutor.submit(() ->
                        this.fetchAlterationCountByGeneByGroup(
                                entry.getKey(),
                                entry.getValue(),
                                enrichmentType,
                                alterationFilter
                            )
                    )
                )
                .map(future -> {
                    try {
                        return future.get();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt(); // Always good to restore interrupted flag
                        throw new RuntimeException("Thread was interrupted", e);
                    } catch (ExecutionException e) {
                        throw new RuntimeException("Unexpected exception during execution", e);
                    }
                })
                .toList();

        results.forEach(alterationCountByGeneAndGroup -> {
            var alterationCountByGenes = alterationCountByGeneAndGroup.getSecond();
            var group = alterationCountByGeneAndGroup.getFirst();

            alterationCountByGenes.forEach(alterationCountByGene -> {
                AlterationEnrichment alterationEnrichment = getOrCreateAlterationEnrichment(
                    alterationEnrichmentByGene,
                    alterationCountByGene
                );

                var countSummary = new CountSummary();
                countSummary.setName(group);
                countSummary.setAlteredCount(alterationCountByGene.getNumberOfAlteredCases());
                countSummary.setProfiledCount(alterationCountByGene.getNumberOfProfiledCases());
                alterationEnrichment.getCounts().add(countSummary);
            });
        });

        var groups = molecularProfileCaseIdentifierByGroup.keySet();

        Collection<AlterationEnrichment> alterationEnrichments = alterationEnrichmentByGene
            .values()
            .stream()
            .map(alterationEnrichment -> {
                addMissingCountsToAlterationEnrichment(alterationEnrichment, groups);
                var pValue = AlterationEnrichmentScoreUtil.calculateEnrichmentScore(
                    alterationEnrichment
                );
                alterationEnrichment.setpValue(pValue);
                return alterationEnrichment;
            })
            .collect(Collectors.toSet());
        return alterationEnrichments;
    }

    private Pair<String, List<AlterationCountByGene>> fetchAlterationCountByGeneByGroup(
        String group,
        List<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers,
        EnrichmentType enrichmentType,
        AlterationFilter alterationFilter
    ) throws MolecularProfileNotFoundException {
        Pair<Set<String>, Set<String>> caseIdsAndMolecularProfileIds =
            this.extractCaseIdsAndMolecularProfiles(molecularProfileCaseIdentifiers);

        List<AlterationCountByGene> alterationCountByGenes = enrichmentType.equals(
                EnrichmentType.SAMPLE
            )
            ? alterationRepository.getAlterationCountByGeneGivenSamplesAndMolecularProfiles(
                caseIdsAndMolecularProfileIds.getFirst(),
                caseIdsAndMolecularProfileIds.getSecond(),
                alterationFilter
            )
            : alterationRepository.getAlterationCountByGeneGivenPatientsAndMolecularProfiles(
                caseIdsAndMolecularProfileIds.getFirst(),
                caseIdsAndMolecularProfileIds.getSecond(),
                alterationFilter
            );
        return Pair.of(group, alterationCountByGenes);
    }

    private Pair<Set<String>, Set<String>> extractCaseIdsAndMolecularProfiles(
        List<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers
    ) throws MolecularProfileNotFoundException {
        Set<String> caseIds = new HashSet<>();
        Set<String> molecularProfileIds = new HashSet<>();
        for (var molecularProfileIdentifier : molecularProfileCaseIdentifiers) {
            String studyId = getStudyIdGivenMolecularProfileId(
                molecularProfileIdentifier.getMolecularProfileId()
            );
            caseIds.add(studyId + "_" + molecularProfileIdentifier.getCaseId());
            molecularProfileIds.add(molecularProfileIdentifier.getMolecularProfileId());
        }
        return Pair.of(caseIds, molecularProfileIds);
    }

    private AlterationEnrichment getOrCreateAlterationEnrichment(
        Map<String, AlterationEnrichment> alterationEnrichmentByGene,
        AlterationCountByGene alterationCountByGene
    ) {
        return alterationEnrichmentByGene.computeIfAbsent(
            alterationCountByGene.getHugoGeneSymbol(),
            key -> {
                AlterationEnrichment enrichment = new AlterationEnrichment();
                enrichment.setEntrezGeneId(alterationCountByGene.getEntrezGeneId());
                enrichment.setHugoGeneSymbol(alterationCountByGene.getHugoGeneSymbol());
                enrichment.setCounts(new ArrayList<>());
                return enrichment;
            }
        );
    }

    private void addMissingCountsToAlterationEnrichment(
        AlterationEnrichment alterationEnrichment,
        Collection<String> groups
    ) {
        Set<String> counts = alterationEnrichment
            .getCounts()
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

    private synchronized String getStudyIdGivenMolecularProfileId(String molecularProfileId)
        throws MolecularProfileNotFoundException {
        if (molecularProfilesMap == null) {
            molecularProfilesMap = alterationRepository
                .getAllMolecularProfiles()
                .stream()
                .collect(
                    Collectors.toMap(MolecularProfile::getStableId, molecularProfile ->
                        molecularProfile
                    )
                );
        }
        var molecularProfile = molecularProfilesMap.get(molecularProfileId);
        if (molecularProfile == null) {
            log.debug("Molecular profile with id {} not found", molecularProfileId);
            throw new MolecularProfileNotFoundException(molecularProfileId);
        }
        return molecularProfile.getCancerStudyIdentifier();
    }
}
