package org.cbioportal.infrastructure.repository.clickhouse.alteration;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.cbioportal.domain.alteration.repository.AlterationRepository;
import org.cbioportal.domain.studyview.StudyViewFilterContext;
import org.cbioportal.legacy.model.AlterationCountByGene;
import org.cbioportal.legacy.model.AlterationFilter;
import org.cbioportal.legacy.model.CopyNumberCountByGene;
import org.cbioportal.legacy.model.EnrichmentType;
import org.cbioportal.legacy.model.GenePanelToGene;
import org.cbioportal.legacy.model.MolecularProfile;
import org.cbioportal.legacy.model.SampleToPanel;
import org.cbioportal.legacy.persistence.helper.AlterationFilterHelper;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("clickhouse")
public class ClickhouseAlterationRepository implements AlterationRepository {

  private final ClickhouseAlterationMapper mapper;

  public ClickhouseAlterationRepository(ClickhouseAlterationMapper mapper) {
    this.mapper = mapper;
  }

  @Override
  public List<AlterationCountByGene> getMutatedGenes(
      StudyViewFilterContext studyViewFilterContext) {
    return mapper.getMutatedGenes(
        studyViewFilterContext,
        AlterationFilterHelper.build(studyViewFilterContext.alterationFilter()));
  }

  @Override
  public List<AlterationCountByGene> getStructuralVariantGenes(
      StudyViewFilterContext studyViewFilterContext) {
    return mapper.getStructuralVariantGenes(
        studyViewFilterContext,
        AlterationFilterHelper.build(studyViewFilterContext.alterationFilter()));
  }

  @Override
  public List<CopyNumberCountByGene> getCnaGenes(StudyViewFilterContext studyViewFilterContext) {
    return mapper.getCnaGenes(
        studyViewFilterContext,
        AlterationFilterHelper.build(studyViewFilterContext.alterationFilter()));
  }

  private Map<String, Map<String, GenePanelToGene>> _data = null;

  public Map<String, Map<String, GenePanelToGene>> getGenePanelsToGenes() {

    if (_data == null) {
      List<GenePanelToGene> genesWithPanels = mapper.getGenePanelGenes();
      Map<String, Map<String, GenePanelToGene>> panelsToGeneMaps =
          genesWithPanels.stream()
              .collect(
                  Collectors.groupingBy(
                      GenePanelToGene::getGenePanelId,
                      Collectors.toMap(
                          GenePanelToGene::getHugoGeneSymbol,
                          panelGene -> panelGene,
                          (existing, replacement) ->
                              existing // handle duplicates by keeping the existing entry
                          )));

      _data = panelsToGeneMaps;
    }
    return _data;
  }

  public List<SampleToPanel> getSampleToGenePanels(
      List<String> sampleStableIds, EnrichmentType enrichmentType) {

    var field = enrichmentType == EnrichmentType.SAMPLE ? "sample_unique_id" : "patient_unique_id";

    return mapper.getSampleToGenePanels(
        sampleStableIds.stream().map(s -> "'" + s + "'").collect(Collectors.joining(",")), field);
  }

  @Override
  public Map<String, Integer> getTotalProfiledCounts(
      StudyViewFilterContext studyViewFilterContext,
      String alterationType,
      List<MolecularProfile> molecularProfiles) {
    return mapper
        .getTotalProfiledCounts(studyViewFilterContext, alterationType, molecularProfiles)
        .stream()
        .collect(
            Collectors.groupingBy(
                AlterationCountByGene::getHugoGeneSymbol,
                Collectors.mapping(
                    AlterationCountByGene::getNumberOfProfiledCases,
                    Collectors.summingInt(Integer::intValue))));
  }

  @Override
  public Map<String, Set<String>> getMatchingGenePanelIds(
      StudyViewFilterContext studyViewFilterContext, String alterationType) {
    return mapper.getMatchingGenePanelIds(studyViewFilterContext, alterationType).stream()
        .collect(
            Collectors.groupingBy(
                GenePanelToGene::getHugoGeneSymbol,
                Collectors.mapping(GenePanelToGene::getGenePanelId, Collectors.toSet())));
  }

  @Override
  public int getSampleProfileCountWithoutPanelData(
      StudyViewFilterContext studyViewFilterContext, String alterationType) {
    return mapper.getSampleProfileCountWithoutPanelData(studyViewFilterContext, alterationType);
  }

  /**
   * @param samples
   * @param molecularProfiles
   * @return
   */
  @Override
  public List<AlterationCountByGene> getAlterationCountByGeneGivenSamplesAndMolecularProfiles(
      Collection<String> samples,
      Collection<String> molecularProfiles,
      AlterationFilter alterationFilter) {
    return mapper.getAlterationCountByGeneGivenSamplesAndMolecularProfiles(
        samples.toArray(new String[0]),
        molecularProfiles.toArray(molecularProfiles.toArray(new String[0])),
        AlterationFilterHelper.build(alterationFilter));
  }

  @Override
  public List<AlterationCountByGene> getAlterationEnrichmentCountsAARON(
      List<String> sampleStableIds,
      List<String> molecularProfiles,
      AlterationFilter alterationFilter) {
    return mapper.getAlterationEnrichmentCountsAARON(
        sampleStableIds.stream().map(s -> "'" + s + "'").collect(Collectors.joining(",")),
        molecularProfiles,
        AlterationFilterHelper.build(alterationFilter));
  }
  ;

  /**
   * @param samples
   * @param molecularProfiles
   * @return
   */
  @Override
  public List<AlterationCountByGene> getAlterationCountByGeneGivenPatientsAndMolecularProfiles(
      Collection<String> samples,
      Collection<String> molecularProfiles,
      AlterationFilter alterationFilter) {
    return mapper.getAlterationCountByGeneGivenPatientsAndMolecularProfiles(
        samples.toArray(new String[0]),
        molecularProfiles.toArray(molecularProfiles.toArray(new String[0])),
        AlterationFilterHelper.build(alterationFilter));
  }

  /**
   * @return
   */
  @Override
  public List<MolecularProfile> getAllMolecularProfiles() {
    return mapper.getAllMolecularProfiles();
  }
}
