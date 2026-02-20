package org.cbioportal.domain.alteration.usecase;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import org.springframework.stereotype.Service;

@Service
public class GetCnaAlterationCountByGeneUseCase extends AbstractAlterationCountByGeneUseCase {

  private final AlterationRepository alterationRepository;
  private final GetFilteredStudyIdsUseCase getFilteredStudyIdsUseCase;
  private final SignificantCopyNumberRegionService significantCopyNumberRegionService;

  public GetCnaAlterationCountByGeneUseCase(
      AlterationRepository alterationRepository,
      GetFilteredMolecularProfilesByAlterationType getFilteredMolecularProfilesByAlterationType,
      GetFilteredStudyIdsUseCase getFilteredStudyIdsUseCase,
      SignificantCopyNumberRegionService significantCopyNumberRegionService) {
    super(alterationRepository, getFilteredMolecularProfilesByAlterationType);

    this.alterationRepository = alterationRepository;
    this.getFilteredStudyIdsUseCase = getFilteredStudyIdsUseCase;
    this.significantCopyNumberRegionService = significantCopyNumberRegionService;
  }

  /**
   * Retrieves a list of genes with copy number alterations (CNA) and their alteration counts for a
   * given filter context.
   *
   * @param studyViewFilterContext Context containing filter criteria.
   * @return List of CopyNumberCountByGene objects representing genes with CNAs.
   * @throws StudyNotFoundException if the specified study is not found.
   */
  public List<CopyNumberCountByGene> execute(StudyViewFilterContext studyViewFilterContext)
      throws StudyNotFoundException {
    var combinedCopyNumberCountByGene =
        combineCopyNumberCountsWithConflictingHugoSymbols(
            alterationRepository.getCnaGenes(studyViewFilterContext));
    return populateAlterationCountsWithCNASigQValue(
        populateAlterationCounts(
            combinedCopyNumberCountByGene,
            studyViewFilterContext,
            AlterationType.COPY_NUMBER_ALTERATION),
        studyViewFilterContext);
  }

  /**
   * Combines alteration counts by Hugo gene symbols. If multiple entries exist for the same gene
   * symbol, their number of altered cases and total counts are summed up. Returns a list of unique
   * AlterationCountByGene objects where each gene symbol is represented only once.
   *
   * <p>This appears in the Data where Genes have similar Hugo Gene Symbols but different Entrez
   * Ids. This is a special case to handle Copy Number Mutations where the Alteration type should be
   * a part of the key
   *
   * @param alterationCounts List of CopyNumberCountByGene objects, potentially with duplicate gene
   *     symbols
   * @return List of AlterationCountByGene objects with unique gene symbols and combined counts
   */
  private List<CopyNumberCountByGene> combineCopyNumberCountsWithConflictingHugoSymbols(
      List<CopyNumberCountByGene> alterationCounts) {
    Map<Pair<String, Integer>, CopyNumberCountByGene> alterationCountByGeneMap = new HashMap<>();
    for (var alterationCount : alterationCounts) {
      var copyNumberKey =
          Pair.create(alterationCount.getHugoGeneSymbol(), alterationCount.getAlteration());
      if (alterationCountByGeneMap.containsKey(copyNumberKey)) {
        CopyNumberCountByGene toUpdate = alterationCountByGeneMap.get(copyNumberKey);
        toUpdate.setNumberOfAlteredCases(
            toUpdate.getNumberOfAlteredCases() + alterationCount.getNumberOfAlteredCases());
        toUpdate.setTotalCount(toUpdate.getTotalCount() + alterationCount.getTotalCount());
      } else {
        alterationCountByGeneMap.put(copyNumberKey, alterationCount);
      }
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
  private List<CopyNumberCountByGene> populateAlterationCountsWithCNASigQValue(
      List<CopyNumberCountByGene> alterationCountByGenes,
      StudyViewFilterContext studyViewFilterContext)
      throws StudyNotFoundException {
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
  private Map<Pair<String, Integer>, Gistic> getGisticMap(
      StudyViewFilterContext studyViewFilterContext) throws StudyNotFoundException {
    var distinctStudyIds = getFilteredStudyIdsUseCase.execute(studyViewFilterContext);
    Map<Pair<String, Integer>, Gistic> gisticMap = new HashMap<>();
    if (distinctStudyIds.size() == 1) {
      var studyId = distinctStudyIds.getFirst();
      List<Gistic> gisticList =
          significantCopyNumberRegionService.getSignificantCopyNumberRegions(
              studyId, Projection.SUMMARY.name(), null, null, null, null);
      AlterationCountServiceUtil.setupGisticMap(gisticList, gisticMap);
    }
    return gisticMap;
  }

  private List<CopyNumberCountByGene> updateAlterationCountsWithCNASigQValue(
      List<CopyNumberCountByGene> alterationCountByGenes,
      Map<Pair<String, Integer>, Gistic> gisticMap) {

    if (!gisticMap.isEmpty()) {
      alterationCountByGenes.parallelStream()
          .filter(
              alterationCount ->
                  gisticMap.containsKey(
                      Pair.create(
                          alterationCount.getHugoGeneSymbol(), alterationCount.getAlteration())))
          .forEach(
              alterationCount ->
                  alterationCount.setqValue(
                      gisticMap
                          .get(
                              Pair.create(
                                  alterationCount.getHugoGeneSymbol(),
                                  alterationCount.getAlteration()))
                          .getqValue()));
    }
    return alterationCountByGenes;
  }
}
