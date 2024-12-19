package org.cbioportal.service.alteration;

import org.apache.commons.math3.util.Pair;
import org.cbioportal.model.AlterationCountByGene;
import org.cbioportal.model.AlterationType;
import org.cbioportal.model.CopyNumberCountByGene;
import org.cbioportal.model.Gistic;
import org.cbioportal.model.MolecularProfile;
import org.cbioportal.model.MutSig;
import org.cbioportal.model.StudyViewFilterContext;
import org.cbioportal.persistence.StudyViewRepository;
import org.cbioportal.service.SignificantCopyNumberRegionService;
import org.cbioportal.service.SignificantlyMutatedGeneService;
import org.cbioportal.service.exception.StudyNotFoundException;
import org.cbioportal.service.util.AlterationCountServiceUtil;
import org.cbioportal.utils.config.annotation.ConditionalOnProperty;
import org.cbioportal.web.parameter.Projection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Implementation of the AlterationCountService interface, providing methods for retrieving and processing
 * alteration counts for samples and patients based on various criteria.
 */
@Service
@ConditionalOnProperty(name = "clickhouse_mode", havingValue = "true")
public class AlterationCountByGeneServiceImpl implements AlterationCountByGeneService {
    private final StudyViewRepository studyViewRepository;
    private final SignificantlyMutatedGeneService significantlyMutatedGeneService;
    private final SignificantCopyNumberRegionService significantCopyNumberRegionService;

    /**
     * Constructor for AlterationCountByGeneServiceImpl.
     *
     * @param studyViewRepository                Repository for study-related queries.
     * @param significantlyMutatedGeneService    Service for retrieving significantly mutated genes.
     * @param significantCopyNumberRegionService Service for retrieving significant copy number regions.
     */
    @Autowired
    public AlterationCountByGeneServiceImpl(StudyViewRepository studyViewRepository, SignificantlyMutatedGeneService significantlyMutatedGeneService, SignificantCopyNumberRegionService significantCopyNumberRegionService) {
        this.studyViewRepository = studyViewRepository;
        this.significantlyMutatedGeneService = significantlyMutatedGeneService;
        this.significantCopyNumberRegionService = significantCopyNumberRegionService;
    }

    /**
     * Retrieves a list of mutated genes and their alteration counts for a given filter context.
     *
     * @param studyViewFilterContext Context containing filter criteria.
     * @return List of AlterationCountByGene objects representing mutated genes.
     * @throws StudyNotFoundException if the specified study is not found.
     */
    @Override
    public List<AlterationCountByGene> getMutatedGenes(StudyViewFilterContext studyViewFilterContext) throws StudyNotFoundException {
        var alterationCountByGenes = populateAlterationCounts(AlterationCountServiceUtil.combineAlterationCountsWithConflictingHugoSymbols(studyViewRepository.getMutatedGenes(studyViewFilterContext)),
            studyViewFilterContext, AlterationType.MUTATION_EXTENDED);
        return populateAlterationCountsWithMutSigQValue(alterationCountByGenes, studyViewFilterContext);
    }

    /**
     * Retrieves a list of genes with copy number alterations (CNA) and their alteration counts for a given filter context.
     *
     * @param studyViewFilterContext Context containing filter criteria.
     * @return List of CopyNumberCountByGene objects representing genes with CNAs.
     * @throws StudyNotFoundException if the specified study is not found.
     */
    public List<CopyNumberCountByGene> getCnaGenes(StudyViewFilterContext studyViewFilterContext) throws StudyNotFoundException {
        var copyNumberAlterationCounts = populateAlterationCounts(AlterationCountServiceUtil.combineCopyNumberCountsWithConflictingHugoSymbols(studyViewRepository.getCnaGenes(studyViewFilterContext)), studyViewFilterContext, AlterationType.COPY_NUMBER_ALTERATION);
        return populateAlterationCountsWithCNASigQValue(copyNumberAlterationCounts, studyViewFilterContext);
    }

    /**
     * Retrieves a list of structural variant genes and their alteration counts for a given filter context.
     *
     * @param studyViewFilterContext Context containing filter criteria.
     * @return List of AlterationCountByGene objects representing structural variant genes.
     * @throws StudyNotFoundException if the specified study is not found.
     */
    @Override
    public List<AlterationCountByGene> getStructuralVariantGenes(StudyViewFilterContext studyViewFilterContext) throws StudyNotFoundException {
        var alterationCountByGenes = populateAlterationCounts(AlterationCountServiceUtil.combineAlterationCountsWithConflictingHugoSymbols(studyViewRepository.getStructuralVariantGenes(studyViewFilterContext)),
            studyViewFilterContext, AlterationType.STRUCTURAL_VARIANT);
        return populateAlterationCountsWithMutSigQValue(alterationCountByGenes, studyViewFilterContext);
    }

    /**
     * Populates alteration counts with profile data, including the total profiled count and matching gene panel IDs.
     *
     * @param alterationCounts       List of alteration counts to enrich.
     * @param studyViewFilterContext Context containing filter criteria.
     * @param alterationType         Type of alteration (e.g., mutation, CNA, structural variant).
     * @param <T>                    The type of alteration count.
     * @return List of enriched alteration counts.
     */
    private <T extends AlterationCountByGene> List<T> populateAlterationCounts(@NonNull List<T> alterationCounts,
                                                                               @NonNull StudyViewFilterContext studyViewFilterContext,
                                                                               @NonNull AlterationType alterationType) {
        final var firstMolecularProfileForEachStudy = getFirstMolecularProfileGroupedByStudy(studyViewFilterContext, alterationType);
        final int totalProfiledCount = studyViewRepository.getTotalProfiledCountsByAlterationType(studyViewFilterContext, alterationType.toString());
        var profiledCountsMap = studyViewRepository.getTotalProfiledCounts(studyViewFilterContext, alterationType.toString(), firstMolecularProfileForEachStudy);
        final var matchingGenePanelIdsMap = studyViewRepository.getMatchingGenePanelIds(studyViewFilterContext, alterationType.toString());
        final int sampleProfileCountWithoutGenePanelData = studyViewRepository.getSampleProfileCountWithoutPanelData(studyViewFilterContext, alterationType.toString());

        alterationCounts.parallelStream()
            .forEach(alterationCountByGene -> {
                String hugoGeneSymbol = alterationCountByGene.getHugoGeneSymbol();
                Set<String> matchingGenePanelIds = matchingGenePanelIdsMap.get(hugoGeneSymbol) != null ?
                    matchingGenePanelIdsMap.get(hugoGeneSymbol) : Collections.emptySet();

                int alterationTotalProfiledCount = AlterationCountServiceUtil.computeTotalProfiledCount(AlterationCountServiceUtil.hasGenePanelData(matchingGenePanelIds),
                    profiledCountsMap.getOrDefault(hugoGeneSymbol, 0),
                    sampleProfileCountWithoutGenePanelData, totalProfiledCount);

                alterationCountByGene.setNumberOfProfiledCases(alterationTotalProfiledCount);

                alterationCountByGene.setMatchingGenePanelIds(matchingGenePanelIds);

            });
        return alterationCounts;
    }

    /**
     * Updates alteration counts with MutSig Q-value data for significance.
     *
     * @param alterationCountByGenes List of alteration counts to update.
     * @param studyViewFilterContext Context containing filter criteria.
     * @return List of alteration counts updated with MutSig Q-value.
     * @throws StudyNotFoundException if the specified study is not found.
     */
    private List<AlterationCountByGene> populateAlterationCountsWithMutSigQValue(List<AlterationCountByGene> alterationCountByGenes, StudyViewFilterContext studyViewFilterContext) throws StudyNotFoundException {
        final var mutSigs = getMutSigs(studyViewFilterContext);
        // If MutSig is not empty update Mutated Genes 
        return AlterationCountServiceUtil.updateAlterationCountsWithMutSigQValue(alterationCountByGenes, mutSigs);
    }

    /**
     * Updates copy number alteration counts with GISTIC significance data.
     *
     * @param alterationCountByGenes List of alteration counts to update.
     * @param studyViewFilterContext Context containing filter criteria.
     * @return List of alteration counts updated with GISTIC significance data.
     * @throws StudyNotFoundException if the specified study is not found.
     */
    private List<CopyNumberCountByGene> populateAlterationCountsWithCNASigQValue(List<CopyNumberCountByGene> alterationCountByGenes, StudyViewFilterContext studyViewFilterContext) throws StudyNotFoundException {
        final var gisticMap = getGisticMap(studyViewFilterContext);
        return AlterationCountServiceUtil.updateAlterationCountsWithCNASigQValue(alterationCountByGenes, gisticMap);
    }

    /**
     * Retrieves the first molecular profile for each study based on the alteration type.
     *
     * @param studyViewFilterContext Context containing filter criteria.
     * @param alterationType Type of alteration (e.g., mutation, CNA, structural variant).
     * @return List of MolecularProfile objects representing the first profile for each study.
     */
    private List<MolecularProfile> getFirstMolecularProfileGroupedByStudy(StudyViewFilterContext studyViewFilterContext, AlterationType alterationType) {
        final var molecularProfiles = studyViewRepository.getFilteredMolecularProfilesByAlterationType(studyViewFilterContext, alterationType.toString());
        return AlterationCountServiceUtil.getFirstMolecularProfileGroupedByStudy(molecularProfiles);
    }

    /**
     * Retrieves MutSig data for significantly mutated genes in the specified studies.
     *
     * @param studyViewFilterContext Context containing filter criteria.
     * @return Map of MutSig objects keyed by Hugo gene symbol.
     * @throws StudyNotFoundException if the specified study is not found.
     */
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

    /**
     * Retrieves GISTIC data for significant copy number alterations in the specified studies.
     *
     * @param studyViewFilterContext Context containing filter criteria.
     * @return Map of GISTIC objects keyed by gene and G-score rank.
     * @throws StudyNotFoundException if the specified study is not found.
     */
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
}
