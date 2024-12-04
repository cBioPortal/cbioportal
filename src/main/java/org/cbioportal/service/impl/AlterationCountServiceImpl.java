package org.cbioportal.service.impl;

import org.apache.commons.math3.util.Pair;
import org.cbioportal.model.AlterationCountBase;
import org.cbioportal.model.AlterationCountByGene;
import org.cbioportal.model.AlterationCountByStructuralVariant;
import org.cbioportal.model.AlterationFilter;
import org.cbioportal.model.AlterationType;
import org.cbioportal.model.CopyNumberCountByGene;
import org.cbioportal.model.Gistic;
import org.cbioportal.model.GisticToGene;
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
import org.cbioportal.service.util.AlterationEnrichmentUtil;
import org.cbioportal.web.parameter.Projection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
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
    
    private static final String WHOLE_EXOME_SEQUENCING = "WES";

    
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
        var alterationCountByGenes = populateAlterationCounts(combineAlterationCountsWithConflictingHugoSymbols( studyViewRepository.getMutatedGenes(studyViewFilterContext)),
            studyViewFilterContext, AlterationType.MUTATION_EXTENDED);
        return populateAlterationCountsWithMutSigQValue(alterationCountByGenes, studyViewFilterContext);
    }
    
    public List<CopyNumberCountByGene> getCnaGenes(StudyViewFilterContext studyViewFilterContext) throws StudyNotFoundException {
        var copyNumberAlterationCounts = populateAlterationCounts(combineCopyNumberCountsWithConflictingHugoSymbols(studyViewRepository.getCnaGenes(studyViewFilterContext)), studyViewFilterContext, AlterationType.COPY_NUMBER_ALTERATION);
        return populateAlterationCountsWithCNASigQValue(copyNumberAlterationCounts, studyViewFilterContext);
    }

    @Override
    public List<AlterationCountByGene> getStructuralVariantGenes(StudyViewFilterContext studyViewFilterContext) throws StudyNotFoundException {
        var alterationCountByGenes = populateAlterationCounts(combineAlterationCountsWithConflictingHugoSymbols(studyViewRepository.getStructuralVariantGenes(studyViewFilterContext)),
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
                
                int alterationTotalProfiledCount =  computeTotalProfiledCount(hasGenePanelData(matchingGenePanelIds), 
                    profiledCountsMap.getOrDefault(hugoGeneSymbol, 0), 
                    sampleProfileCountWithoutGenePanelData, totalProfiledCount);
                
                alterationCountByGene.setNumberOfProfiledCases(alterationTotalProfiledCount);

                alterationCountByGene.setMatchingGenePanelIds(matchingGenePanelIds);
                
            }); 
        return alterationCounts;
    }
    
    private int computeTotalProfiledCount(boolean hasGenePanelData, int alterationsProfiledCount, int sampleProfileCountWithoutGenePanelData, int totalProfiledCount) {
        int profiledCount = hasGenePanelData ? alterationsProfiledCount + sampleProfileCountWithoutGenePanelData
                    : sampleProfileCountWithoutGenePanelData;
        return profiledCount == 0 ? totalProfiledCount : profiledCount;
    }

    private List<AlterationCountByGene> populateAlterationCountsWithMutSigQValue(List<AlterationCountByGene> alterationCountByGenes, StudyViewFilterContext studyViewFilterContext) throws StudyNotFoundException {
        final var mutSigs = getMutSigs(studyViewFilterContext);
        // If MutSig is not empty update Mutated Genes 
        if (!mutSigs.isEmpty()) {
            alterationCountByGenes.parallelStream()
                .filter(alterationCount -> mutSigs.containsKey(alterationCount.getHugoGeneSymbol()))
                .forEach(alterationCount ->
                    alterationCount.setqValue(mutSigs.get(alterationCount.getHugoGeneSymbol()).getqValue())
                );
        }
        return alterationCountByGenes;
    }

    private List<CopyNumberCountByGene> populateAlterationCountsWithCNASigQValue(List<CopyNumberCountByGene> alterationCountByGenes, StudyViewFilterContext studyViewFilterContext) throws StudyNotFoundException {
        final var gisticMap = getGisticMap(studyViewFilterContext);
        
        if(!gisticMap.isEmpty()) {
            alterationCountByGenes.parallelStream()
                .filter(alterationCount -> gisticMap.containsKey(Pair.create(alterationCount.getHugoGeneSymbol(), alterationCount.getAlteration())))
                .forEach(alterationCount -> {
                    alterationCount.setqValue(gisticMap.get(Pair.create(alterationCount.getHugoGeneSymbol(), alterationCount.getAlteration())).getqValue());
                });
        }
       return alterationCountByGenes; 
    }
    
   private List<MolecularProfile> getFirstMolecularProfileGroupedByStudy(StudyViewFilterContext studyViewFilterContext, AlterationType alterationType) {
        final var molecularProfiles = studyViewRepository.getFilteredMolecularProfilesByAlterationType(studyViewFilterContext, alterationType.toString());

       return molecularProfiles.stream()
           .collect(Collectors.toMap(
               MolecularProfile::getCancerStudyIdentifier,
               Function.identity(),
               (existing, replacement) -> existing  // Keep the first occurrence
           ))
           .values()
           .stream()
           .toList();
   } 
    
    /**
     * Combines alteration counts by Hugo gene symbols. If multiple entries exist for the same 
     * gene symbol, their number of altered cases and total counts are summed up. Returns a 
     * list of unique AlterationCountByGene objects where each gene symbol is represented only once.
     * 
     * This appears in the Data where Genes have similar Hugo Gene Symbols but different Entrez Ids
     *
     * @param alterationCounts List of AlterationCountByGene objects, potentially with duplicate gene symbols
     * @return List of AlterationCountByGene objects with unique gene symbols and combined counts
     */
    private List<AlterationCountByGene> combineAlterationCountsWithConflictingHugoSymbols(@NonNull List<AlterationCountByGene> alterationCounts) {
        Map<String, AlterationCountByGene> alterationCountByGeneMap = new HashMap<>();
        for (var alterationCount : alterationCounts) {
            if (alterationCountByGeneMap.containsKey(alterationCount.getHugoGeneSymbol())){
                AlterationCountByGene toUpdate = alterationCountByGeneMap.get(alterationCount.getHugoGeneSymbol());
                toUpdate.setNumberOfAlteredCases(toUpdate.getNumberOfAlteredCases() + alterationCount.getNumberOfAlteredCases());
                toUpdate.setTotalCount(toUpdate.getTotalCount() + alterationCount.getTotalCount());
            } else {
                alterationCountByGeneMap.put(alterationCount.getHugoGeneSymbol(), alterationCount);
            }
        }
        return alterationCountByGeneMap.values().stream().toList(); 
    }

    /**
     * Combines alteration counts by Hugo gene symbols. If multiple entries exist for the same 
     * gene symbol, their number of altered cases and total counts are summed up. Returns a 
     * list of unique AlterationCountByGene objects where each gene symbol is represented only once.
     *
     * This appears in the Data where Genes have similar Hugo Gene Symbols but different Entrez Ids.
     * This is a special case to handle Copy Number Mutations where the Alteration type should be a part of the key
     *
     * @param alterationCounts List of CopyNumberCountByGene objects, potentially with duplicate gene symbols
     * @return List of AlterationCountByGene objects with unique gene symbols and combined counts
     */
    private List<CopyNumberCountByGene> combineCopyNumberCountsWithConflictingHugoSymbols(@NonNull List<CopyNumberCountByGene> alterationCounts) {
        Map<Pair<String, Integer>, CopyNumberCountByGene> alterationCountByGeneMap = new HashMap<>();
        for (var alterationCount : alterationCounts) {
            var copyNumberKey = Pair.create(alterationCount.getHugoGeneSymbol(), alterationCount.getAlteration());
            if (alterationCountByGeneMap.containsKey(copyNumberKey)) {
                AlterationCountByGene toUpdate = alterationCountByGeneMap.get(copyNumberKey);
                toUpdate.setNumberOfAlteredCases(toUpdate.getNumberOfAlteredCases() + alterationCount.getNumberOfAlteredCases());
                toUpdate.setTotalCount(toUpdate.getTotalCount() + alterationCount.getTotalCount());
            } else {
                alterationCountByGeneMap.put(copyNumberKey, alterationCount);
            }
        }
        return alterationCountByGeneMap.values().stream().toList();
    }
    
    private boolean hasGenePanelData(@NonNull Set<String> matchingGenePanelIds) {
        return matchingGenePanelIds.contains(WHOLE_EXOME_SEQUENCING) 
            && matchingGenePanelIds.size() > 1 || !matchingGenePanelIds.contains(WHOLE_EXOME_SEQUENCING) && !matchingGenePanelIds.isEmpty();
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
            for(Gistic gistic : gisticList) {
                var amp = (gistic.getAmp()) ? 2 : -2;
                for (GisticToGene gene : gistic.getGenes()) {
                    var key = Pair.create(gene.getHugoGeneSymbol(), amp);
                    Gistic currentGistic = gisticMap.get(key);
                    if (currentGistic == null || gistic.getqValue().compareTo(currentGistic.getqValue()) < 0) {
                        gisticMap.put(key, gistic);
                    }
                }
            }
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
                    studyAlterationCountByGenes.forEach(datum -> {
                        String key = datum.getUniqueEventKey();
                        if (totalResult.containsKey(key)) {
                            S alterationCountByGene = totalResult.get(key);
                            alterationCountByGene.setTotalCount(alterationCountByGene.getTotalCount() + datum.getTotalCount());
                            alterationCountByGene.setNumberOfAlteredCases(alterationCountByGene.getNumberOfAlteredCases() + datum.getNumberOfAlteredCases());
                            alterationCountByGene.setNumberOfProfiledCases(alterationCountByGene.getNumberOfProfiledCases() + datum.getNumberOfProfiledCases());
                            Set<String> matchingGenePanelIds = new HashSet<>();
                            if (!alterationCountByGene.getMatchingGenePanelIds().isEmpty()) {
                                matchingGenePanelIds.addAll(alterationCountByGene.getMatchingGenePanelIds());
                            }
                            if (!datum.getMatchingGenePanelIds().isEmpty()) {
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
