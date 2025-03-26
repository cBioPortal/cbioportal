package org.cbioportal.domain.alteration.usecase;

import org.apache.commons.math3.util.Pair;
import org.cbioportal.domain.alteration.repository.AlterationRepository;
import org.cbioportal.domain.cancerstudy.usecase.GetFilteredStudyIdsUseCase;
import org.cbioportal.domain.generic_assay.usecase.GetFilteredMolecularProfilesByAlterationType;
import org.cbioportal.domain.studyview.StudyViewFilterContext;
import org.cbioportal.legacy.model.AlterationType;
import org.cbioportal.legacy.model.CopyNumberCountByGene;
import org.cbioportal.legacy.model.Gistic;
import org.cbioportal.legacy.service.SignificantCopyNumberRegionService;
import org.cbioportal.legacy.service.exception.StudyNotFoundException;
import org.cbioportal.legacy.service.util.AlterationCountServiceUtil;
import org.cbioportal.legacy.web.parameter.Projection;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@Profile("clickhouse")
public class GetCnaAlterationCountByGeneUseCase extends AbstractAlterationCountByGeneUseCase{

    private final AlterationRepository alterationRepository;
    private final GetFilteredStudyIdsUseCase getFilteredStudyIdsUseCase;
    private final SignificantCopyNumberRegionService significantCopyNumberRegionService;

    public GetCnaAlterationCountByGeneUseCase(AlterationRepository alterationRepository,
                                              GetFilteredMolecularProfilesByAlterationType getFilteredMolecularProfilesByAlterationType, GetFilteredStudyIdsUseCase getFilteredStudyIdsUseCase, SignificantCopyNumberRegionService significantCopyNumberRegionService){
        super(alterationRepository,getFilteredMolecularProfilesByAlterationType);

        this.alterationRepository = alterationRepository;
        this.getFilteredStudyIdsUseCase = getFilteredStudyIdsUseCase;
        this.significantCopyNumberRegionService = significantCopyNumberRegionService;
    }

    /**
     * Retrieves a list of genes with copy number alterations (CNA) and their alteration counts for a given filter context.
     *
     * @param studyViewFilterContext Context containing filter criteria.
     * @return List of CopyNumberCountByGene objects representing genes with CNAs.
     * @throws StudyNotFoundException if the specified study is not found.
     */
    public List<CopyNumberCountByGene> execute(StudyViewFilterContext studyViewFilterContext) throws StudyNotFoundException {
        var combinedCopyNumberCountByGene =
                combineCopyNumberCountsWithConflictingHugoSymbols(alterationRepository.getCnaGenes(studyViewFilterContext));
        return populateAlterationCountsWithCNASigQValue(
                populateAlterationCounts(combinedCopyNumberCountByGene,studyViewFilterContext,
                        AlterationType.COPY_NUMBER_ALTERATION),
                studyViewFilterContext);
    }

    /**
     * Combines copy number alteration counts by Hugo gene symbols across multiple studies.
     * If multiple entries exist for the same gene symbol and alteration type, their counts are combined.
     * Additionally, tracks which studies each gene-alteration pair is altered in.
     * <p>
     * This handles cases where genes have the same Hugo Gene Symbol but different Entrez IDs,
     * and the special case of copy number alterations where alteration type is part of the key.
     *
     * @param alterationCounts List of CopyNumberCountByGene objects, potentially from multiple studies
     * @return List of CopyNumberCountByGene objects with unique gene-alteration pairs and combined counts
     */
    private List<CopyNumberCountByGene> combineCopyNumberCountsWithConflictingHugoSymbols(List<CopyNumberCountByGene> alterationCounts) {
        // Map to store unique gene-alteration entries with combined counts
        Map<Pair<String, Integer>, CopyNumberCountByGene> alterationCountByGeneMap = new HashMap<>();
        // Map to track which studies each gene-alteration pair is altered in
        Map<Pair<String, Integer>, Set<String>> geneAltToStudyIdsMap = new HashMap<>();

        for (var alterationCount : alterationCounts) {
            String hugoGeneSymbol = alterationCount.getHugoGeneSymbol();
            Integer alteration = alterationCount.getAlteration();
            String studyId = alterationCount.getStudyId();

            // Create a composite key of gene symbol and alteration type
            var copyNumberKey = Pair.create(hugoGeneSymbol, alteration);

            // If we've seen this gene-alteration pair before, update its counts
            if (alterationCountByGeneMap.containsKey(copyNumberKey)) {
                CopyNumberCountByGene toUpdate = alterationCountByGeneMap.get(copyNumberKey);
                toUpdate.setNumberOfAlteredCases(toUpdate.getNumberOfAlteredCases() + alterationCount.getNumberOfAlteredCases());
                toUpdate.setTotalCount(toUpdate.getTotalCount() + alterationCount.getTotalCount());
            } else {
                // First time seeing this gene-alteration pair, add it to our map
                alterationCountByGeneMap.put(copyNumberKey, alterationCount);
            }

            // Track that this gene-alteration pair is altered in this study
            geneAltToStudyIdsMap.computeIfAbsent(copyNumberKey, k -> new HashSet<>()).add(studyId);
        }

        // Set the list of studies each gene-alteration pair is altered in
        for (Map.Entry<Pair<String, Integer>, CopyNumberCountByGene> entry : alterationCountByGeneMap.entrySet()) {
            Pair<String, Integer> key = entry.getKey();
            alterationCountByGeneMap.get(key).setAlteredInStudyIds(geneAltToStudyIdsMap.get(key));
        }

        return alterationCountByGeneMap.values().stream().toList();
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
        return updateAlterationCountsWithCNASigQValue(alterationCountByGenes, gisticMap);
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

    private List<CopyNumberCountByGene> updateAlterationCountsWithCNASigQValue(
            List<CopyNumberCountByGene> alterationCountByGenes,
            Map<Pair<String, Integer>, Gistic> gisticMap) {

        if (!gisticMap.isEmpty()) {
            alterationCountByGenes.parallelStream()
                    .filter(alterationCount -> gisticMap.containsKey(Pair.create(alterationCount.getHugoGeneSymbol(), alterationCount.getAlteration())))
                    .forEach(alterationCount ->
                            alterationCount.setqValue(gisticMap.get(Pair.create(alterationCount.getHugoGeneSymbol(), alterationCount.getAlteration())).getqValue())
                    );
        }
        return alterationCountByGenes;
    }
}
