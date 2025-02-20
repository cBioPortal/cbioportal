package org.cbioportal.domain.alteration.usecase;

import org.cbioportal.domain.alteration.repository.AlterationRepository;
import org.cbioportal.domain.cancerstudy.usecase.GetFilteredStudyIdsUseCase;
import org.cbioportal.domain.generic_assay.usecase.GetFilteredMolecularProfilesByAlterationType;
import org.cbioportal.domain.studyview.StudyViewFilterContext;
import org.cbioportal.legacy.model.AlterationCountByGene;
import org.cbioportal.legacy.model.AlterationType;
import org.cbioportal.legacy.model.MutSig;
import org.cbioportal.legacy.service.SignificantlyMutatedGeneService;
import org.cbioportal.legacy.service.exception.StudyNotFoundException;
import org.cbioportal.legacy.web.parameter.Projection;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Profile("clickhouse")
public class GetAlterationCountByGeneUseCase extends AbstractAlterationCountByGeneUseCase {
    private final AlterationRepository alterationRepository;
    private final GetFilteredStudyIdsUseCase getFilteredStudyIdsUseCase;
    private final SignificantlyMutatedGeneService significantlyMutatedGeneService;
    public GetAlterationCountByGeneUseCase(AlterationRepository alterationRepository, GetFilteredMolecularProfilesByAlterationType getFilteredMolecularProfilesByAlterationType, GetFilteredStudyIdsUseCase getFilteredStudyIdsUseCase, SignificantlyMutatedGeneService significantlyMutatedGeneService) {
        super(alterationRepository, getFilteredMolecularProfilesByAlterationType);

        this.alterationRepository = alterationRepository;
        this.getFilteredStudyIdsUseCase = getFilteredStudyIdsUseCase;
        this.significantlyMutatedGeneService = significantlyMutatedGeneService;
    }

    /**
     * Retrieves alteration counts by gene based on the given {@link AlterationType} and study filter context.
     * Supports {@code MUTATION_EXTENDED} and {@code STRUCTURAL_VARIANT} alteration types.
     *
     * @param studyViewFilterContext the context containing study view filters
     * @param alterationType the type of alteration to retrieve (must be either {@code MUTATION_EXTENDED} or {@code STRUCTURAL_VARIANT})
     * @return a list of {@link AlterationCountByGene} objects containing alteration counts
     * @throws StudyNotFoundException if the study is not found
     * @throws UnsupportedOperationException if the given {@code alterationType} is not supported
     */
    public List<AlterationCountByGene> execute(StudyViewFilterContext studyViewFilterContext,
                                               AlterationType alterationType) throws StudyNotFoundException {

        final List<AlterationCountByGene> alterationCountByGenes = switch (alterationType) {
            case MUTATION_EXTENDED -> alterationRepository.getMutatedGenes(studyViewFilterContext);
            case STRUCTURAL_VARIANT -> alterationRepository.getStructuralVariantGenes(studyViewFilterContext);
            default -> throw new UnsupportedOperationException("AlterationType " + alterationType + " not supported.." +
                    ". For cna... use GetCnaAlterationCountByGeneUseCase");
        };

        var combinedAlterationCountByGenes =
               combineAlterationCountsWithConflictingHugoSymbols(alterationCountByGenes);

        return populateAlterationCountsWithMutSigQValue(
                populateAlterationCounts(
                       combinedAlterationCountByGenes,
                        studyViewFilterContext, alterationType),
                studyViewFilterContext);
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
    private List<AlterationCountByGene> combineAlterationCountsWithConflictingHugoSymbols(List<AlterationCountByGene> alterationCounts) {
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
        return updateAlterationCountsWithMutSigQValue(alterationCountByGenes, mutSigs);
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

    private List<AlterationCountByGene> updateAlterationCountsWithMutSigQValue(
            List<AlterationCountByGene> alterationCountByGenes,
            Map<String, MutSig> mutSigs) {

        if (!mutSigs.isEmpty()) {
            alterationCountByGenes.parallelStream()
                    .filter(alterationCount -> mutSigs.containsKey(alterationCount.getHugoGeneSymbol()))
                    .forEach(alterationCount ->
                            alterationCount.setqValue(mutSigs.get(alterationCount.getHugoGeneSymbol()).getqValue())
                    );
        }
        return alterationCountByGenes;
    }


}
