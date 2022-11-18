package org.cbioportal.service.impl;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.math3.util.Pair;
import org.cbioportal.model.*;
import org.cbioportal.model.util.Select;
import org.cbioportal.persistence.AlterationRepository;
import org.cbioportal.persistence.MolecularProfileRepository;
import org.cbioportal.service.AlterationCountService;
import org.cbioportal.service.util.AlterationEnrichmentUtil;
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
    private AlterationEnrichmentUtil<AlterationCountByStructuralVariant> alterationEnrichmentUtilStructVar;
    @Autowired
    private MolecularProfileRepository molecularProfileRepository;

    @Override
    public Pair<List<AlterationCountByGene>, Long> getSampleAlterationGeneCounts(List<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers,
                                                                                 Select<Integer> entrezGeneIds,
                                                                                 boolean includeFrequency,
                                                                                 boolean includeMissingAlterationsFromGenePanel,
                                                                                 AlterationFilter alterationFilter) {

        Function<List<MolecularProfileCaseIdentifier>, List<AlterationCountByGene>> dataFetcher = profileCaseIdentifiers ->
            alterationRepository.getSampleAlterationGeneCounts(new TreeSet<>(profileCaseIdentifiers), entrezGeneIds, alterationFilter);

        BiFunction<List<MolecularProfileCaseIdentifier>, List<AlterationCountByGene>, Long> includeFrequencyFunction =
            (a, b) -> alterationEnrichmentUtil.includeFrequencyForSamples(a, b, includeMissingAlterationsFromGenePanel);

        return getAlterationGeneCounts(
            molecularProfileCaseIdentifiers,
            includeFrequency,
            dataFetcher,
            includeFrequencyFunction
        );
    }

    @Override
    public Pair<List<AlterationCountByGene>, Long> getPatientAlterationGeneCounts(List<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers,
                                                                                  Select<Integer> entrezGeneIds,
                                                                                  boolean includeFrequency,
                                                                                  boolean includeMissingAlterationsFromGenePanel,
                                                                                  AlterationFilter alterationFilter) {

        Function<List<MolecularProfileCaseIdentifier>, List<AlterationCountByGene>> dataFetcher = profileCaseIdentifiers ->
            alterationRepository.getPatientAlterationGeneCounts(new TreeSet<>(profileCaseIdentifiers), entrezGeneIds, alterationFilter);

        BiFunction<List<MolecularProfileCaseIdentifier>, List<AlterationCountByGene>, Long> includeFrequencyFunction =
            (a, b) -> alterationEnrichmentUtil.includeFrequencyForPatients(a, b, includeMissingAlterationsFromGenePanel);

        return getAlterationGeneCounts(
            molecularProfileCaseIdentifiers,
            includeFrequency,
            dataFetcher,
            includeFrequencyFunction
        );
    }

    @Override
    public Pair<List<AlterationCountByGene>, Long> getSampleMutationGeneCounts(List<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers,
                                                                               Select<Integer> entrezGeneIds,
                                                                               boolean includeFrequency,
                                                                               boolean includeMissingAlterationsFromGenePanel,
                                                                               AlterationFilter alterationFilter) {
        return getSampleAlterationGeneCounts(molecularProfileCaseIdentifiers,
            entrezGeneIds,
            includeFrequency,
            includeMissingAlterationsFromGenePanel,
            alterationFilter
        );
    }

    @Override
    public Pair<List<AlterationCountByGene>, Long> getPatientMutationGeneCounts(List<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers,
                                                                                Select<Integer> entrezGeneIds,
                                                                                boolean includeFrequency,
                                                                                boolean includeMissingAlterationsFromGenePanel,
                                                                                AlterationFilter alterationFilter) {
        return getPatientAlterationGeneCounts(molecularProfileCaseIdentifiers,
            entrezGeneIds,
            includeFrequency,
            includeMissingAlterationsFromGenePanel,
            alterationFilter);
    }

    @Override
    public Pair<List<AlterationCountByGene>, Long> getSampleStructuralVariantGeneCounts(List<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers,
                                                                                        Select<Integer> entrezGeneIds,
                                                                                        boolean includeFrequency,
                                                                                        boolean includeMissingAlterationsFromGenePanel,
                                                                                        AlterationFilter alterationFilter) {
        return getSampleAlterationGeneCounts(molecularProfileCaseIdentifiers,
            entrezGeneIds,
            includeFrequency,
            includeMissingAlterationsFromGenePanel,
            alterationFilter
        );
    }

    @Override
    public Pair<List<AlterationCountByGene>, Long> getPatientStructuralVariantGeneCounts(List<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers,
                                                                                         Select<Integer> entrezGeneIds,
                                                                                         boolean includeFrequency,
                                                                                         boolean includeMissingAlterationsFromGenePanel,
                                                                                         AlterationFilter alterationFilter) {
        return getPatientAlterationGeneCounts(molecularProfileCaseIdentifiers,
            entrezGeneIds,
            includeFrequency,
            includeMissingAlterationsFromGenePanel,
            alterationFilter
        );
    }

    @Override
    public Pair<List<AlterationCountByStructuralVariant>, Long> getSampleStructuralVariantCounts(List<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers,
                                                                                                 boolean includeFrequency,
                                                                                                 boolean includeMissingAlterationsFromGenePanel,
                                                                                                 AlterationFilter alterationFilter) {
                                                                                                 
        Function<List<MolecularProfileCaseIdentifier>, List<AlterationCountByStructuralVariant>> dataFetcher = profileCaseIdentifiers ->
            alterationRepository.getSampleStructuralVariantCounts(new TreeSet<>(profileCaseIdentifiers), alterationFilter);

        BiFunction<List<MolecularProfileCaseIdentifier>, List<AlterationCountByStructuralVariant>, Long> includeFrequencyFunction =
            (a, b) -> alterationEnrichmentUtilStructVar.includeFrequencyForSamples(a, b, includeMissingAlterationsFromGenePanel);
            
        return getAlterationGeneCounts(
            molecularProfileCaseIdentifiers,
            includeFrequency,
            dataFetcher,
            includeFrequencyFunction
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
    public Pair<List<CopyNumberCountByGene>, Long> getSampleCnaGeneCounts(List<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers,
                                                                          Select<Integer> entrezGeneIds,
                                                                          boolean includeFrequency,
                                                                          boolean includeMissingAlterationsFromGenePanel,
                                                                          AlterationFilter alterationFilter) {

        Function<List<MolecularProfileCaseIdentifier>, List<CopyNumberCountByGene>> dataFetcher = profileCaseIdentifiers ->
            alterationRepository.getSampleCnaGeneCounts(new TreeSet<>(profileCaseIdentifiers), entrezGeneIds, alterationFilter);

        BiFunction<List<MolecularProfileCaseIdentifier>, List<CopyNumberCountByGene>, Long> includeFrequencyFunction =
            (a, b) -> alterationEnrichmentUtilCna.includeFrequencyForSamples(a, b, includeMissingAlterationsFromGenePanel);

        Function<CopyNumberCountByGene, String> keyGenerator = d -> d.getEntrezGeneId().toString() + d.getAlteration().toString();

        return getAlterationGeneCounts(
            molecularProfileCaseIdentifiers,
            includeFrequency,
            dataFetcher,
            includeFrequencyFunction
        );
    }

    @Override
    public Pair<List<CopyNumberCountByGene>, Long> getPatientCnaGeneCounts(List<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers,
                                                                           Select<Integer> entrezGeneIds,
                                                                           boolean includeFrequency,
                                                                           boolean includeMissingAlterationsFromGenePanel,
                                                                           AlterationFilter alterationFilter) {

        Function<List<MolecularProfileCaseIdentifier>, List<CopyNumberCountByGene>> dataFetcher = profileCaseIdentifiers ->
            alterationRepository.getPatientCnaGeneCounts(new TreeSet<>(profileCaseIdentifiers), entrezGeneIds, alterationFilter);

        BiFunction<List<MolecularProfileCaseIdentifier>, List<CopyNumberCountByGene>, Long> includeFrequencyFunction =
            (a, b) -> alterationEnrichmentUtilCna.includeFrequencyForPatients(a, b, includeMissingAlterationsFromGenePanel);

        Function<CopyNumberCountByGene, String> keyGenerator = d -> d.getEntrezGeneId().toString() + d.getAlteration().toString();

        return getAlterationGeneCounts(
            molecularProfileCaseIdentifiers,
            includeFrequency,
            dataFetcher,
            includeFrequencyFunction
        );
    }

    private <S extends AlterationCountBase> Pair<List<S>, Long> getAlterationGeneCounts(
        List<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers,
        boolean includeFrequency,
        Function<List<MolecularProfileCaseIdentifier>, List<S>> dataFetcher,
        BiFunction<List<MolecularProfileCaseIdentifier>, List<S>, Long> includeFrequencyFunction) {

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

            Map<String, S> totalResult = new HashMap<>();

            molecularProfileCaseIdentifiers
                .stream()
                .collect(Collectors
                    .groupingBy(identifier -> molecularProfileIdStudyIdMap.get(identifier.getMolecularProfileId())))
                .values()
                .forEach(studyMolecularProfileCaseIdentifiers -> {
                    List<S> studyAlterationCountByGenes = dataFetcher.apply(studyMolecularProfileCaseIdentifiers);
                    if (includeFrequency) {
                        Long studyProfiledCasesCount = includeFrequencyFunction.apply(studyMolecularProfileCaseIdentifiers, studyAlterationCountByGenes);
                        profiledCasesCount.updateAndGet(v -> v + studyProfiledCasesCount);
                    }
                    studyAlterationCountByGenes.forEach(datum -> {
                        String key = datum.getUniqueEventKey();
                        if (totalResult.containsKey(key)) {
                            S alterationCountByGene = totalResult.get(key);
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
                            totalResult.put(key, alterationCountByGene);
                        } else {
                            totalResult.put(key, datum);
                        }
                    });
                });
            alterationCountByGenes = new ArrayList<>(totalResult.values());
        }
        return new Pair<>(alterationCountByGenes, profiledCasesCount.get());
    }

}
