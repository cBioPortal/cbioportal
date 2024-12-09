package org.cbioportal.service.impl;

import org.apache.commons.math3.util.Pair;
import org.cbioportal.model.AlterationCountBase;
import org.cbioportal.model.AlterationCountByGene;
import org.cbioportal.model.AlterationCountByStructuralVariant;
import org.cbioportal.model.AlterationFilter;
import org.cbioportal.model.AlterationType;
import org.cbioportal.model.CopyNumberCountByGene;
import org.cbioportal.model.Gistic;
import org.cbioportal.model.MolecularProfile;
import org.cbioportal.model.MolecularProfileCaseIdentifier;
import org.cbioportal.model.MutSig;
import org.cbioportal.model.StudyViewFilterContext;
import org.cbioportal.model.util.Select;
import org.cbioportal.persistence.AlterationRepository;
import org.cbioportal.persistence.MolecularProfileRepository;
import org.cbioportal.persistence.StudyViewRepository;
import org.cbioportal.service.AlterationCountService;
import org.cbioportal.service.SignificantCopyNumberRegionService;
import org.cbioportal.service.SignificantlyMutatedGeneService;
import org.cbioportal.service.exception.StudyNotFoundException;
import org.cbioportal.service.util.AlterationCountServiceUtil;
import org.cbioportal.service.util.AlterationEnrichmentUtil;
import org.cbioportal.web.parameter.Projection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class AlterationCountServiceImpl implements AlterationCountService {

    private final AlterationRepository alterationRepository;
    private final AlterationEnrichmentUtil<AlterationCountByGene> alterationEnrichmentUtil;
    private final AlterationEnrichmentUtil<CopyNumberCountByGene> alterationEnrichmentUtilCna;
    private final AlterationEnrichmentUtil<AlterationCountByStructuralVariant> alterationEnrichmentUtilStructVar;
    private final MolecularProfileRepository molecularProfileRepository;
    private final SignificantlyMutatedGeneService significantlyMutatedGeneService;
    private final StudyViewRepository studyViewRepository;
    private final SignificantCopyNumberRegionService significantCopyNumberRegionService;

    
    @Autowired
    public AlterationCountServiceImpl(AlterationRepository alterationRepository, AlterationEnrichmentUtil<AlterationCountByGene> alterationEnrichmentUtil,
                                      AlterationEnrichmentUtil<CopyNumberCountByGene> alterationEnrichmentUtilCna,
                                      AlterationEnrichmentUtil<AlterationCountByStructuralVariant> alterationEnrichmentUtilStructVar,
                                      MolecularProfileRepository molecularProfileRepository,
                                      StudyViewRepository studyViewRepository, SignificantlyMutatedGeneService significantlyMutatedGeneService,
                                      SignificantCopyNumberRegionService significantCopyNumberRegionService) {
        this.alterationRepository = alterationRepository;
        this.alterationEnrichmentUtil = alterationEnrichmentUtil;
        this.alterationEnrichmentUtilCna = alterationEnrichmentUtilCna;
        this.alterationEnrichmentUtilStructVar = alterationEnrichmentUtilStructVar;
        this.molecularProfileRepository = molecularProfileRepository;
        this.studyViewRepository = studyViewRepository;
        this.significantlyMutatedGeneService = significantlyMutatedGeneService;
        this.significantCopyNumberRegionService = significantCopyNumberRegionService;
    }
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

    @Override
    public List<AlterationCountByGene> getMutatedGenes(StudyViewFilterContext studyViewFilterContext) throws StudyNotFoundException {
        var alterationCountByGenes = populateAlterationCounts(AlterationCountServiceUtil.combineAlterationCountsWithConflictingHugoSymbols( studyViewRepository.getMutatedGenes(studyViewFilterContext)),
            studyViewFilterContext, AlterationType.MUTATION_EXTENDED);
        return populateAlterationCountsWithMutSigQValue(alterationCountByGenes, studyViewFilterContext);
    }
    
    public List<CopyNumberCountByGene> getCnaGenes(StudyViewFilterContext studyViewFilterContext) throws StudyNotFoundException {
        var copyNumberAlterationCounts = populateAlterationCounts(AlterationCountServiceUtil.combineCopyNumberCountsWithConflictingHugoSymbols(studyViewRepository.getCnaGenes(studyViewFilterContext)), studyViewFilterContext, AlterationType.COPY_NUMBER_ALTERATION);
        return populateAlterationCountsWithCNASigQValue(copyNumberAlterationCounts, studyViewFilterContext);
    }

    @Override
    public List<AlterationCountByGene> getStructuralVariantGenes(StudyViewFilterContext studyViewFilterContext) throws StudyNotFoundException {
        var alterationCountByGenes = populateAlterationCounts(AlterationCountServiceUtil.combineAlterationCountsWithConflictingHugoSymbols(studyViewRepository.getStructuralVariantGenes(studyViewFilterContext)),
            studyViewFilterContext, AlterationType.STRUCTURAL_VARIANT);
        return populateAlterationCountsWithMutSigQValue(alterationCountByGenes, studyViewFilterContext);
    }
    
    private < T extends AlterationCountByGene> List<T> populateAlterationCounts(@NonNull List<T> alterationCounts,
                                                                                @NonNull StudyViewFilterContext studyViewFilterContext,
                                                                                @NonNull AlterationType alterationType) {
        final var firstMolecularProfileForEachStudy = getFirstMolecularProfileGroupedByStudy(studyViewFilterContext, alterationType);
        final int totalProfiledCount = studyViewRepository.getTotalProfiledCountsByAlterationType(studyViewFilterContext, alterationType.toString());
        var profiledCountsMap = studyViewRepository.getTotalProfiledCounts(studyViewFilterContext, alterationType.toString(), firstMolecularProfileForEachStudy);
        final var matchingGenePanelIdsMap = studyViewRepository.getMatchingGenePanelIds(studyViewFilterContext, alterationType.toString());
        final int sampleProfileCountWithoutGenePanelData = studyViewRepository.getSampleProfileCountWithoutPanelData(studyViewFilterContext, alterationType.toString());
        
        alterationCounts.parallelStream()
            .forEach(alterationCountByGene ->  {
                String hugoGeneSymbol = alterationCountByGene.getHugoGeneSymbol();
                Set<String> matchingGenePanelIds = matchingGenePanelIdsMap.get(hugoGeneSymbol) != null ?
                    matchingGenePanelIdsMap.get(hugoGeneSymbol) : Collections.emptySet();
                
                int alterationTotalProfiledCount =  AlterationCountServiceUtil.computeTotalProfiledCount(AlterationCountServiceUtil.hasGenePanelData(matchingGenePanelIds), 
                    profiledCountsMap.getOrDefault(hugoGeneSymbol, 0), 
                    sampleProfileCountWithoutGenePanelData, totalProfiledCount);
                
                alterationCountByGene.setNumberOfProfiledCases(alterationTotalProfiledCount);

                alterationCountByGene.setMatchingGenePanelIds(matchingGenePanelIds);
                
            }); 
        return alterationCounts;
    }

    private List<AlterationCountByGene> populateAlterationCountsWithMutSigQValue(List<AlterationCountByGene> alterationCountByGenes, StudyViewFilterContext studyViewFilterContext) throws StudyNotFoundException {
        final var mutSigs = getMutSigs(studyViewFilterContext);
        // If MutSig is not empty update Mutated Genes 
        return AlterationCountServiceUtil.updateAlterationCountsWithMutSigQValue(alterationCountByGenes, mutSigs);
    }

    private List<CopyNumberCountByGene> populateAlterationCountsWithCNASigQValue(List<CopyNumberCountByGene> alterationCountByGenes, StudyViewFilterContext studyViewFilterContext) throws StudyNotFoundException {
        final var gisticMap = getGisticMap(studyViewFilterContext);
        
        return AlterationCountServiceUtil.updateAlterationCountsWithCNASigQValue(alterationCountByGenes, gisticMap);
    }
    
   private List<MolecularProfile> getFirstMolecularProfileGroupedByStudy(StudyViewFilterContext studyViewFilterContext, AlterationType alterationType) {
        final var molecularProfiles = studyViewRepository.getFilteredMolecularProfilesByAlterationType(studyViewFilterContext, alterationType.toString());

       return AlterationCountServiceUtil.getFirstMolecularProfileGroupedByStudy(molecularProfiles);
   }

    private Map<String, MutSig> getMutSigs(StudyViewFilterContext studyViewFilterContext) throws StudyNotFoundException {
        var distinctStudyIds = studyViewRepository.getFilteredStudyIds(studyViewFilterContext);
        Map<String, MutSig> mutSigs = new HashMap<>();
        if (distinctStudyIds.size() == 1) {
            var studyId = distinctStudyIds.getFirst();
            mutSigs = significantlyMutatedGeneService.getSignificantlyMutatedGenes(
                    studyId,
                    Projection.SUMMARY.name(),
                    null,
                    null,
                    null,
                    null)
                .stream()
                .collect(Collectors.toMap(MutSig::getHugoGeneSymbol, Function.identity()));
        }
        return mutSigs;
    }
    
    private Map<Pair<String, Integer>, Gistic> getGisticMap(StudyViewFilterContext studyViewFilterContext) throws StudyNotFoundException {
        var distinctStudyIds = studyViewRepository.getFilteredStudyIds(studyViewFilterContext);
        Map<Pair<String, Integer>, Gistic> gisticMap = new HashMap<>(); 
        if (distinctStudyIds.size() == 1) {
            var studyId = distinctStudyIds.getFirst();
            List<Gistic> gisticList = significantCopyNumberRegionService.getSignificantCopyNumberRegions(
                studyId,
                Projection.SUMMARY.name(),
                null,
                null,
                null,
                null);
            AlterationCountServiceUtil.setupGisticMap(gisticList, gisticMap);
        }
        return gisticMap;
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
                    AlterationCountServiceUtil.setupAlterationGeneCountsMap(studyAlterationCountByGenes, totalResult);
                });
            alterationCountByGenes = new ArrayList<>(totalResult.values());
        }
        return new Pair<>(alterationCountByGenes, profiledCasesCount.get());
    }

}
