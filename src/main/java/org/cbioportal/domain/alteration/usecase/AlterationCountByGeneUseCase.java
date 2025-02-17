package org.cbioportal.domain.alteration.usecase;

import org.apache.commons.math3.util.Pair;
import org.cbioportal.domain.alteration.repository.AlterationRepository;
import org.cbioportal.domain.cancerstudy.usecase.GetFilteredStudyIdsUseCase;
import org.cbioportal.domain.generic_assay.usecase.GetFilteredMolecularProfilesByAlterationType;
import org.cbioportal.legacy.model.AlterationCountByGene;
import org.cbioportal.legacy.model.AlterationType;
import org.cbioportal.legacy.model.CopyNumberCountByGene;
import org.cbioportal.legacy.model.Gistic;
import org.cbioportal.legacy.model.MolecularProfile;
import org.cbioportal.legacy.model.MutSig;
import org.cbioportal.legacy.service.SignificantCopyNumberRegionService;
import org.cbioportal.legacy.service.SignificantlyMutatedGeneService;
import org.cbioportal.legacy.service.exception.StudyNotFoundException;
import org.cbioportal.legacy.service.util.AlterationCountServiceUtil;
import org.cbioportal.legacy.web.parameter.Projection;
import org.cbioportal.domain.studyview.StudyViewFilterContext;
import org.springframework.context.annotation.Profile;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Profile("clickhouse")
public class AlterationCountByGeneUseCase {

    private final AlterationRepository alterationRepository;
    private final GetFilteredMolecularProfilesByAlterationType getFilteredMolecularProfilesByAlterationType;
    private final GetFilteredStudyIdsUseCase getFilteredStudyIdsUseCase;
    private final SignificantlyMutatedGeneService significantlyMutatedGeneService;
    private final SignificantCopyNumberRegionService significantCopyNumberRegionService;

    public AlterationCountByGeneUseCase(AlterationRepository alterationRepository, GetFilteredMolecularProfilesByAlterationType getFilteredMolecularProfilesByAlterationType,
                                        GetFilteredStudyIdsUseCase getFilteredStudyIdsUseCase,
                                        SignificantlyMutatedGeneService significantlyMutatedGeneService,
                                        SignificantCopyNumberRegionService significantCopyNumberRegionService) {
        this.alterationRepository = alterationRepository;
        this.getFilteredMolecularProfilesByAlterationType = getFilteredMolecularProfilesByAlterationType;
        this.getFilteredStudyIdsUseCase = getFilteredStudyIdsUseCase;
        this.significantlyMutatedGeneService = significantlyMutatedGeneService;
        this.significantCopyNumberRegionService = significantCopyNumberRegionService;
    }

    public List<AlterationCountByGene> getMutatedGenes(StudyViewFilterContext studyViewFilterContext) throws StudyNotFoundException{
        var alterationCountByGenes =
                populateAlterationCounts(AlterationCountServiceUtil.combineAlterationCountsWithConflictingHugoSymbols(alterationRepository.getMutatedGenes(studyViewFilterContext)),
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
        var copyNumberAlterationCounts =
                populateAlterationCounts(AlterationCountServiceUtil.combineCopyNumberCountsWithConflictingHugoSymbols(alterationRepository.getCnaGenes(studyViewFilterContext)), studyViewFilterContext, AlterationType.COPY_NUMBER_ALTERATION);
        return populateAlterationCountsWithCNASigQValue(copyNumberAlterationCounts, studyViewFilterContext);
    }

    /**
     * Retrieves a list of structural variant genes and their alteration counts for a given filter context.
     *
     * @param studyViewFilterContext Context containing filter criteria.
     * @return List of AlterationCountByGene objects representing structural variant genes.
     * @throws StudyNotFoundException if the specified study is not found.
     */
    public List<AlterationCountByGene> getStructuralVariantGenes(StudyViewFilterContext studyViewFilterContext) throws StudyNotFoundException {
        var alterationCountByGenes =
                populateAlterationCounts(AlterationCountServiceUtil.combineAlterationCountsWithConflictingHugoSymbols(alterationRepository.getStructuralVariantGenes(studyViewFilterContext)),
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
        final var firstMolecularProfileForEachStudy = getFirstMolecularProfileGroupedByStudy(studyViewFilterContext,
                alterationType);
        final int totalProfiledCount = alterationRepository.getTotalProfiledCountsByAlterationType(studyViewFilterContext,
                alterationType.toString());
        var profiledCountsMap = alterationRepository.getTotalProfiledCounts(studyViewFilterContext, alterationType.toString(),
                firstMolecularProfileForEachStudy);
        final var matchingGenePanelIdsMap = alterationRepository.getMatchingGenePanelIds(studyViewFilterContext,
                alterationType.toString());
        final int sampleProfileCountWithoutGenePanelData =
                alterationRepository.getSampleProfileCountWithoutPanelData(studyViewFilterContext, alterationType.toString());

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
        final var molecularProfiles =
                getFilteredMolecularProfilesByAlterationType.execute(studyViewFilterContext, alterationType.toString());
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
        var distinctStudyIds = getFilteredStudyIdsUseCase.execute(studyViewFilterContext);
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
        var distinctStudyIds = getFilteredStudyIdsUseCase.execute(studyViewFilterContext);
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
