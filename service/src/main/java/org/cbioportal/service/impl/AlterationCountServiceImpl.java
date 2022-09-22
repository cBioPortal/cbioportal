package org.cbioportal.service.impl;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.math3.util.Pair;
import org.cbioportal.model.*;
import org.cbioportal.model.util.Select;
import org.cbioportal.persistence.AlterationRepository;
import org.cbioportal.persistence.MolecularProfileRepository;
import org.cbioportal.service.AlterationCountService;
import org.cbioportal.service.util.AlterationEnrichmentUtil;
import org.cbioportal.service.util.MolecularProfileUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class AlterationCountServiceImpl implements AlterationCountService {

    @Autowired
    private AlterationRepository alterationRepository;
    @Autowired
    private AlterationEnrichmentUtil<AlterationCountByGene> alterationEnrichmentUtil;
    @Autowired
    private AlterationEnrichmentUtil<CopyNumberCountByGene> alterationEnrichmentUtilCna;
    @Autowired
    private MolecularProfileUtil molecularProfileUtil;
    @Autowired
    private MolecularProfileRepository molecularProfileRepository;

    @Override
    public Pair<List<AlterationCountByGene>, Long> getSampleAlterationCounts(List<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers,
                                                                             Select<Integer> entrezGeneIds,
                                                                             boolean includeFrequency,
                                                                             boolean includeMissingAlterationsFromGenePanel,
                                                                             AlterationFilter alterationFilter) {

        Function<List<MolecularProfileCaseIdentifier>, List<AlterationCountByGene>> dataFetcher = profileCaseIdentifiers ->
            alterationRepository.getSampleAlterationCounts(new TreeSet<>(profileCaseIdentifiers), entrezGeneIds, alterationFilter);

        BiFunction<List<MolecularProfileCaseIdentifier>, List<AlterationCountByGene>, Long> includeFrequencyFunction =
            (a, b) -> alterationEnrichmentUtil.includeFrequencyForSamples(a, b, includeMissingAlterationsFromGenePanel);

        Function<AlterationCountByGene, String> keyGenerator = d -> d.getEntrezGeneId().toString();

        return getAlterationCounts(
            molecularProfileCaseIdentifiers,
            includeFrequency,
            dataFetcher,
            includeFrequencyFunction,
            keyGenerator);
    }

    @Override
    public Pair<List<AlterationCountByGene>, Long> getPatientAlterationCounts(List<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers,
                                                                              Select<Integer> entrezGeneIds,
                                                                              boolean includeFrequency,
                                                                              boolean includeMissingAlterationsFromGenePanel,
                                                                              AlterationFilter alterationFilter) {

        Function<List<MolecularProfileCaseIdentifier>, List<AlterationCountByGene>> dataFetcher = profileCaseIdentifiers ->
            alterationRepository.getPatientAlterationCounts(profileCaseIdentifiers, entrezGeneIds, alterationFilter);

        BiFunction<List<MolecularProfileCaseIdentifier>, List<AlterationCountByGene>, Long> includeFrequencyFunction =
            (a, b) -> alterationEnrichmentUtil.includeFrequencyForPatients(a, b, includeMissingAlterationsFromGenePanel);

        Function<AlterationCountByGene, String> keyGenerator = d -> d.getEntrezGeneId().toString();

        return getAlterationCounts(
            molecularProfileCaseIdentifiers,
            includeFrequency,
            dataFetcher,
            includeFrequencyFunction,
            keyGenerator);
    }

    @Override
    public Pair<List<AlterationCountByGene>, Long> getSampleMutationCounts(List<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers,
                                                                           Select<Integer> entrezGeneIds,
                                                                           boolean includeFrequency,
                                                                           boolean includeMissingAlterationsFromGenePanel,
                                                                           AlterationFilter alterationFilter) {
        return getSampleAlterationCounts(molecularProfileCaseIdentifiers,
            entrezGeneIds,
            includeFrequency,
            includeMissingAlterationsFromGenePanel,
            alterationFilter
        );
    }

    @Override
    public Pair<List<AlterationCountByGene>, Long> getPatientMutationCounts(List<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers,
                                                                            Select<Integer> entrezGeneIds,
                                                                            boolean includeFrequency,
                                                                            boolean includeMissingAlterationsFromGenePanel,
                                                                            AlterationFilter alterationFilter) {
        return getPatientAlterationCounts(molecularProfileCaseIdentifiers,
            entrezGeneIds,
            includeFrequency,
            includeMissingAlterationsFromGenePanel,
            alterationFilter);
    }

    @Override
    public Pair<List<AlterationCountByGene>, Long> getSampleStructuralVariantCounts(List<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers,
                                                                                    Select<Integer> entrezGeneIds,
                                                                                    boolean includeFrequency,
                                                                                    boolean includeMissingAlterationsFromGenePanel,
                                                                                    AlterationFilter alterationFilter) {
        return getSampleAlterationCounts(molecularProfileCaseIdentifiers,
            entrezGeneIds,
            includeFrequency,
            includeMissingAlterationsFromGenePanel,
            alterationFilter
        );
    }

    @Override
    public Pair<List<AlterationCountByGene>, Long> getPatientStructuralVariantCounts(List<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers,
                                                                                     Select<Integer> entrezGeneIds,
                                                                                     boolean includeFrequency,
                                                                                     boolean includeMissingAlterationsFromGenePanel,
                                                                                     AlterationFilter alterationFilter) {
        return getPatientAlterationCounts(molecularProfileCaseIdentifiers,
            entrezGeneIds,
            includeFrequency,
            includeMissingAlterationsFromGenePanel,
            alterationFilter
        );
    }

// -- Should be reinstated when the legacy CNA count endpoint retires            
//    @Override
//    public List<AlterationCountByGene> getSampleCnaCounts(List<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers,
//                                                          Select<Integer> entrezGeneIds,
//                                                          boolean includeFrequency,
//                                                          boolean includeMissingAlterationsFromGenePanel,
//                                                          AlterationEventTypeFilter alterationFilter) {
//        return getSampleAlterationCounts(molecularProfileCaseIdentifiers,
//            entrezGeneIds,
//            includeFrequency,
//            includeMissingAlterationsFromGenePanel,
//            new ArrayList<>(),
//            alterationFilter);
//    }
//
//    @Override
//    public List<AlterationCountByGene> getPatientCnaCounts(List<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers,
//                                                           List<Integer> entrezGeneIds,
//                                                           boolean includeFrequency,
//                                                           boolean includeMissingAlterationsFromGenePanel,
//                                                           AlterationEventTypeFilter alterationFilter) {
//        return getPatientAlterationCounts(molecularProfileCaseIdentifiers,
//            entrezGeneIds,
//            includeFrequency,
//            includeMissingAlterationsFromGenePanel,
//            new ArrayList<>(),
//            alterationFilter);
//    }

    @Override
    public Pair<List<CopyNumberCountByGene>, Long> getSampleCnaCounts(List<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers,
                                                                      Select<Integer> entrezGeneIds,
                                                                      boolean includeFrequency,
                                                                      boolean includeMissingAlterationsFromGenePanel,
                                                                      AlterationFilter alterationFilter) {

        Function<List<MolecularProfileCaseIdentifier>, List<CopyNumberCountByGene>> dataFetcher = profileCaseIdentifiers ->
            alterationRepository.getSampleCnaCounts(new TreeSet<>(profileCaseIdentifiers), entrezGeneIds, alterationFilter);

        BiFunction<List<MolecularProfileCaseIdentifier>, List<CopyNumberCountByGene>, Long> includeFrequencyFunction =
            (a, b) -> alterationEnrichmentUtilCna.includeFrequencyForSamples(a, b, includeMissingAlterationsFromGenePanel);

        Function<CopyNumberCountByGene, String> keyGenerator = d -> d.getEntrezGeneId().toString() + d.getAlteration().toString();

        return getAlterationCounts(
            molecularProfileCaseIdentifiers,
            includeFrequency,
            dataFetcher,
            includeFrequencyFunction,
            keyGenerator);
    }

    @Override
    public Pair<List<CopyNumberCountByGene>, Long> getPatientCnaCounts(List<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers,
                                                                       Select<Integer> entrezGeneIds,
                                                                       boolean includeFrequency,
                                                                       boolean includeMissingAlterationsFromGenePanel,
                                                                       AlterationFilter alterationFilter) {

        Function<List<MolecularProfileCaseIdentifier>, List<CopyNumberCountByGene>> dataFetcher = profileCaseIdentifiers ->
            alterationRepository.getPatientCnaCounts(profileCaseIdentifiers, entrezGeneIds, alterationFilter);

        BiFunction<List<MolecularProfileCaseIdentifier>, List<CopyNumberCountByGene>, Long> includeFrequencyFunction =
            (a, b) -> alterationEnrichmentUtilCna.includeFrequencyForPatients(a, b, includeMissingAlterationsFromGenePanel);

        Function<CopyNumberCountByGene, String> keyGenerator = d -> d.getEntrezGeneId().toString() + d.getAlteration().toString();

        return getAlterationCounts(
            molecularProfileCaseIdentifiers,
            includeFrequency,
            dataFetcher,
            includeFrequencyFunction,
            keyGenerator);
    }

    private <S extends AlterationCountByGene> Pair<List<S>, Long> getAlterationCounts(
        List<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers,
        boolean includeFrequency,
        Function<List<MolecularProfileCaseIdentifier>, List<S>> dataFetcher,
        BiFunction<List<MolecularProfileCaseIdentifier>, List<S>, Long> includeFrequencyFunction,
        Function<S, String> keyGenerator) {

        List<S> alterationCountByGenes;
        AtomicReference<Long> profiledCasesCount = new AtomicReference<>(0L);
        if (molecularProfileCaseIdentifiers.isEmpty()) {
            alterationCountByGenes = Collections.emptyList();
        } else {
            Set<String> molecularProfileIds = molecularProfileCaseIdentifiers
                .stream()
                .map(MolecularProfileCaseIdentifier::getMolecularProfileId)
                .collect(Collectors.toSet());
            
            Map<String, String> molecularProfileIdStudyIdMap = molecularProfileRepository
                .getMolecularProfiles(molecularProfileIds, "SUMMARY")
                .stream()
                .collect(Collectors.toMap(MolecularProfile::getStableId, MolecularProfile::getCancerStudyIdentifier));

            Map<String, List<MolecularProfileCaseIdentifier>> groupedByMolecularProfileIds = molecularProfileCaseIdentifiers
                .stream()
                .collect(Collectors
                    .groupingBy(identifier -> molecularProfileIdStudyIdMap.get(identifier.getMolecularProfileId()))
                );

            Map<String, List<S>> studyAlterationCountByMolecularProfileIds = groupedByMolecularProfileIds
                .entrySet()
                .stream()
                .collect(Collectors.toMap(
                    e -> e.getKey(),
                    e -> dataFetcher.apply(e.getValue())
                ));

            if (includeFrequency) {
                // TODO should we make this step optional with an additional parameter?
                // make sure all groups have alteration count for the exact same genes 
                // even if alteration count may be zero for certain studies
                this.addAlterationCountsForAllGenes(studyAlterationCountByMolecularProfileIds);
            
                studyAlterationCountByMolecularProfileIds.forEach((molecularProfileId, studyAlterationCountByGenes) -> {
                    List<MolecularProfileCaseIdentifier> studyMolecularProfileCaseIdentifiers = 
                        groupedByMolecularProfileIds.get(molecularProfileId);
                    Long studyProfiledCasesCount = includeFrequencyFunction.apply(
                        studyMolecularProfileCaseIdentifiers, studyAlterationCountByGenes);
                    profiledCasesCount.updateAndGet(v -> v + studyProfiledCasesCount);
                });
            }

            alterationCountByGenes = this.calculateAggregatedAlterationCounts(
                studyAlterationCountByMolecularProfileIds,
                keyGenerator
            );
        }
        return new Pair<>(alterationCountByGenes, profiledCasesCount.get());
    }

    /**
     * Make sure that each molecular profile has alteration counts for the exact same genes by adding
     * a missing AlterationCountByGene with zero alteration count where necessary.
     * 
     * @param studyAlterationCountByMolecularProfileIds alteration counts per gene grouped by molecular profile id
     * @param <S> alteration count for a single gene
     */
    private <S extends AlterationCountByGene> void addAlterationCountsForAllGenes(
        Map<String, List<S>> studyAlterationCountByMolecularProfileIds
    ) {
        Map<Integer, String> entrezGeneIdToHugoSymbol = studyAlterationCountByMolecularProfileIds
            .values()
            .stream()
            .flatMap(Collection::stream)
            // Collectors.toMap throws exception when hugo gene symbol is null, 
            // we need a custom collect function to keep null values as is 
            // .collect(Collectors.toMap(S::getEntrezGeneId, S::getHugoGeneSymbol, (a, b) -> a));
            .collect(HashMap::new, (map, count) -> map.put(count.getEntrezGeneId(), count.getHugoGeneSymbol()), HashMap::putAll);

        studyAlterationCountByMolecularProfileIds.forEach((molecularProfileId, studyAlterationCountByGenes) -> {
            Map<Integer, S> map = studyAlterationCountByGenes.stream().collect(Collectors.toMap(S::getEntrezGeneId, c -> c));
            entrezGeneIdToHugoSymbol.forEach((entrezGeneId, hugoGeneSymbol) -> {
                // gene has zero alteration count for this study
                // add a new alteration count object for this gene with zero count
                if (!map.containsKey(entrezGeneId)) {
                    AlterationCountByGene missingGeneAlterationCount = new AlterationCountByGene();
                    missingGeneAlterationCount.setEntrezGeneId(entrezGeneId);
                    missingGeneAlterationCount.setHugoGeneSymbol(hugoGeneSymbol);
                    missingGeneAlterationCount.setTotalCount(0);
                    missingGeneAlterationCount.setNumberOfAlteredCases(0);

                    // ideally we should be instantiating an object of type <S> instead of the base type to avoid this
                    // unsafe cast, but it's not straightforward to do that without knowing the actual runtime class
                    studyAlterationCountByGenes.add((S) missingGeneAlterationCount);
                }
            });
        });
    }

    /**
     * Aggregates alteration and profiled case counts for each gene from multiple studies.
     * 
     * @param studyAlterationCountByMolecularProfileIds alteration counts per gene grouped by molecular profile id
     * @param keyGenerator unique key generator for AlterationCountByGene
     * @return a list of AlterationCountByGene where each gene has only one corresponding AlterationCountByGene
     * @param <S> alteration count for a single gene
     */
    private <S extends AlterationCountByGene> List<S> calculateAggregatedAlterationCounts(
        Map<String, List<S>> studyAlterationCountByMolecularProfileIds,
        Function<S, String> keyGenerator
    ) {
        Map<String, S> aggregatedAlterationCounts = new HashMap<>();

        studyAlterationCountByMolecularProfileIds
            .forEach((molecularProfileId, studyAlterationCountByGenes) -> {
                studyAlterationCountByGenes.forEach(datum -> {
                    String key = keyGenerator.apply(datum);
                    if (aggregatedAlterationCounts.containsKey(key)) {
                        S alterationCountByGene = aggregatedAlterationCounts.get(key);
                        alterationCountByGene.setTotalCount(alterationCountByGene.getTotalCount() + datum.getTotalCount());
                        alterationCountByGene.setNumberOfAlteredCases(alterationCountByGene.getNumberOfAlteredCases() + datum.getNumberOfAlteredCases());
                        alterationCountByGene.setNumberOfProfiledCases(alterationCountByGene.getNumberOfProfiledCases() + datum.getNumberOfProfiledCases());
                        Set<String> matchingGenePanelIds = new HashSet<>();
                        if (CollectionUtils.isNotEmpty(alterationCountByGene.getMatchingGenePanelIds())) {
                            matchingGenePanelIds.addAll(alterationCountByGene.getMatchingGenePanelIds());
                        }
                        if (CollectionUtils.isNotEmpty(datum.getMatchingGenePanelIds())) {
                            matchingGenePanelIds.addAll(datum.getMatchingGenePanelIds());
                        }
                        alterationCountByGene.setMatchingGenePanelIds(matchingGenePanelIds);
                        aggregatedAlterationCounts.put(key, alterationCountByGene);
                    } else {
                        aggregatedAlterationCounts.put(key, datum);
                    }
                });
            });
        
        return new ArrayList<>(aggregatedAlterationCounts.values());
    }
}
