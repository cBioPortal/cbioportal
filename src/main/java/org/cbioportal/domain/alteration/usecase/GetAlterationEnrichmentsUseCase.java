package org.cbioportal.domain.alteration.usecase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import org.cbioportal.domain.alteration.repository.AlterationRepository;
import org.cbioportal.domain.alteration.util.AlterationEnrichmentScoreUtil;
import org.cbioportal.legacy.model.AlterationCountByGene;
import org.cbioportal.legacy.model.AlterationEnrichment;
import org.cbioportal.legacy.model.AlterationFilter;
import org.cbioportal.legacy.model.CountSummary;
import org.cbioportal.legacy.model.EnrichmentType;
import org.cbioportal.legacy.model.MolecularProfile;
import org.cbioportal.legacy.model.MolecularProfileCaseIdentifier;
import org.cbioportal.legacy.model.SampleToPanel;
import org.cbioportal.legacy.service.exception.MolecularProfileNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

@Service
@Profile("clickhouse")
public class GetAlterationEnrichmentsUseCase {

  private static final Logger log = LoggerFactory.getLogger(GetAlterationEnrichmentsUseCase.class);

  private Map<String, MolecularProfile> molecularProfilesMap;

  private final AlterationRepository alterationRepository;
  private final AsyncTaskExecutor threadPoolTaskExecutor;

  public GetAlterationEnrichmentsUseCase(
      AlterationRepository alterationRepository, AsyncTaskExecutor threadPoolTaskExecutor) {
    this.alterationRepository = alterationRepository;
    this.threadPoolTaskExecutor = threadPoolTaskExecutor;
  }

  public Collection<AlterationEnrichment> execute(
      Map<String, List<MolecularProfileCaseIdentifier>> molecularProfileCaseIdentifierByGroup,
      EnrichmentType enrichmentType,
      AlterationFilter alterationFilter) {
    Map<String, AlterationEnrichment> alterationEnrichmentByGene = new HashMap<>();

    // here aaron
    List<Pair<String, List<AlterationCountByGene>>> results =
        molecularProfileCaseIdentifierByGroup.entrySet().stream()
            .map(
                entry ->
                    threadPoolTaskExecutor.submit(
                        () ->
                            this.fetchAlterationCountByGeneByGroup(
                                entry.getKey(),
                                entry.getValue(),
                                enrichmentType,
                                alterationFilter)))
            .map(
                future -> {
                  try {
                    return future.get();
                  } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); // Always good to restore interrupted flag
                    throw new RuntimeException("Thread was interrupted", e);
                  } catch (ExecutionException e) {
                    throw new RuntimeException("Unexpected exception during execution", e);
                  }
                })
            .toList();

    results.forEach(
        alterationCountByGeneAndGroup -> {
          var alterationCountByGenes = alterationCountByGeneAndGroup.getSecond();
          var group = alterationCountByGeneAndGroup.getFirst();

          alterationCountByGenes.forEach(
              alterationCountByGene -> {
                AlterationEnrichment alterationEnrichment =
                    getOrCreateAlterationEnrichment(
                        alterationEnrichmentByGene, alterationCountByGene);

                var countSummary = new CountSummary();
                countSummary.setName(group);
                countSummary.setAlteredCount(alterationCountByGene.getNumberOfAlteredCases());
                countSummary.setProfiledCount(alterationCountByGene.getNumberOfProfiledCases());
                alterationEnrichment.getCounts().add(countSummary);
              });
        });

    var groups = molecularProfileCaseIdentifierByGroup.keySet();

    return alterationEnrichmentByGene.values().stream()
        .filter(
            alterationEnrichment -> {
              // Filter out genes where all alteredCount values are zero
              return alterationEnrichment.getCounts().stream()
                  .anyMatch(countSummary -> countSummary.getAlteredCount() > 0);
            })
        .map(
            alterationEnrichment -> {
              addMissingCountsToAlterationEnrichment(alterationEnrichment, groups);
              var pValue =
                  AlterationEnrichmentScoreUtil.calculateEnrichmentScore(alterationEnrichment);
              alterationEnrichment.setpValue(pValue);
              return alterationEnrichment;
            })
        .filter(
            alterationEnrichment -> {
              // Filter out genes where all alteredCount values are zero
              return alterationEnrichment.getCounts().stream()
                  .anyMatch(countSummary -> countSummary.getAlteredCount() > 0);
            })
        .collect(Collectors.toSet());
  }

  private Pair<String, List<AlterationCountByGene>> fetchAlterationCountByGeneByGroupBK(
      String group,
      List<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers,
      EnrichmentType enrichmentType,
      AlterationFilter alterationFilter)
      throws MolecularProfileNotFoundException {
    Pair<Set<String>, Set<String>> caseIdsAndMolecularProfileIds =
        this.extractCaseIdsAndMolecularProfiles(molecularProfileCaseIdentifiers);

    List<AlterationCountByGene> alterationCountByGenes =
        enrichmentType.equals(EnrichmentType.SAMPLE)
            ? alterationRepository.getAlterationCountByGeneGivenSamplesAndMolecularProfiles(
                caseIdsAndMolecularProfileIds.getFirst(),
                caseIdsAndMolecularProfileIds.getSecond(),
                alterationFilter)
            : alterationRepository.getAlterationCountByGeneGivenPatientsAndMolecularProfiles(
                caseIdsAndMolecularProfileIds.getFirst(),
                caseIdsAndMolecularProfileIds.getSecond(),
                alterationFilter);
    return Pair.of(group, alterationCountByGenes);
  }

  private Pair<String, List<AlterationCountByGene>> fetchAlterationCountByGeneByGroup(
      String group,
      List<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers,
      EnrichmentType enrichmentType,
      AlterationFilter alterationFilter)
      throws MolecularProfileNotFoundException {
    Pair<Set<String>, Set<String>> caseIdsAndMolecularProfileIds =
        this.extractCaseIdsAndMolecularProfiles(molecularProfileCaseIdentifiers);

    // we need a map of panels to genes which are profiled by them
    var panelToGeneMap = alterationRepository.getGenePanelsToGenes();

    List<String> sampleStableIds = new ArrayList<>(caseIdsAndMolecularProfileIds.getFirst());
    List<String> molecularProfileIds = new ArrayList<>(caseIdsAndMolecularProfileIds.getSecond());

    List<SampleToPanel> sampleToGenePanels =
        alterationRepository.getSampleToGenePanels(sampleStableIds);
    // group the panels by the sample ids which they are associated with
    // this tells us for each sample, what gene panels were applied
    var samplesToPanelMap =
        sampleToGenePanels.stream()
            .collect(
                Collectors.groupingBy(
                    SampleToPanel::getSampleUniqueId,
                    Collectors.mapping(e -> e.getGenePanelId(), Collectors.toSet())));

    // many of the samples are governed by the same combination of panels
    // we want to group the samples by a key that represents the set of panels applied
    Map<String, List<String>> clumps =
        samplesToPanelMap.keySet().stream()
            .collect(
                Collectors.groupingBy(
                    sampleId ->
                        samplesToPanelMap.get(sampleId).stream().collect(Collectors.joining(","))));

    List<String> molecularProfileIdsList =
        new ArrayList<>(caseIdsAndMolecularProfileIds.getSecond());
    List<String> sampleStableIdsList = new ArrayList<>(caseIdsAndMolecularProfileIds.getFirst());

    List<AlterationCountByGene> alterationCounts =
        alterationRepository.getAlterationEnrichmentCountsAARON(
            sampleStableIdsList, molecularProfileIdsList, alterationFilter);

    HashMap<String, AlterationCountByGene> alteredGenesWithCounts = new HashMap();

    // we need map of genes to alteration counts
    alterationCounts.stream()
        .forEach(
            (alterationCountByGene) -> {
              String hugoGeneSymbol = alterationCountByGene.getHugoGeneSymbol();
              int entrezGeneId = alterationCountByGene.getEntrezGeneId();

              int count = alterationCountByGene.getNumberOfAlteredCases();
              if (!alteredGenesWithCounts.containsKey(hugoGeneSymbol)) {
                var acg = new AlterationCountByGene();
                acg.setHugoGeneSymbol(hugoGeneSymbol);
                acg.setEntrezGeneId(entrezGeneId);
                acg.setNumberOfAlteredCases(0);
                alteredGenesWithCounts.put(hugoGeneSymbol, acg);
              }
              // add the count to existing tally
              alteredGenesWithCounts
                  .get(hugoGeneSymbol)
                  .setNumberOfAlteredCases(
                      count + alteredGenesWithCounts.get(hugoGeneSymbol).getNumberOfAlteredCases());
            });

    var geneCount = new HashMap<String, AlterationCountByGene>();

    clumps.entrySet().stream()
        .forEach(
            entry -> {
              var geneLists =
                  Arrays.stream(entry.getKey().split(","))
                      .map(panelId -> panelToGeneMap.get(panelId))
                      .collect(Collectors.toList());

              Set<String> mergeGenes =
                  geneLists.stream()
                      .map(Map::keySet)
                      .reduce(
                          (set1, set2) -> {
                            set1.retainAll(set2);
                            return set1;
                          })
                      .orElse(Collections.emptySet());

              mergeGenes.stream()
                  .forEach(
                      gene -> {
                        if (geneCount.containsKey(gene)) {
                          var count = geneCount.get(gene);
                          count.setNumberOfProfiledCases(
                              count.getNumberOfProfiledCases() + entry.getValue().size());
                        } else {
                          var alterationCountByGene = new AlterationCountByGene();
                          alterationCountByGene.setHugoGeneSymbol(gene);

                          alterationCountByGene.setNumberOfProfiledCases(entry.getValue().size());
                          alterationCountByGene.setNumberOfAlteredCases(0);
                          geneCount.put(gene, alterationCountByGene);
                        }
                      });
            });

    geneCount.entrySet().stream()
        .forEach(
            n -> {
              if (alteredGenesWithCounts.containsKey(n.getKey())) {
                n.getValue()
                    .setNumberOfAlteredCases(
                        alteredGenesWithCounts.get(n.getKey()).getNumberOfAlteredCases());
              }
            });

    return Pair.of(group, geneCount.values().stream().toList());
  }

  private Pair<Set<String>, Set<String>> extractCaseIdsAndMolecularProfiles(
      List<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers)
      throws MolecularProfileNotFoundException {
    Set<String> caseIds = new HashSet<>();
    Set<String> molecularProfileIds = new HashSet<>();
    for (var molecularProfileIdentifier : molecularProfileCaseIdentifiers) {
      String studyId =
          getStudyIdGivenMolecularProfileId(molecularProfileIdentifier.getMolecularProfileId());
      caseIds.add(studyId + "_" + molecularProfileIdentifier.getCaseId());
      molecularProfileIds.add(molecularProfileIdentifier.getMolecularProfileId());
    }
    return Pair.of(caseIds, molecularProfileIds);
  }

  private AlterationEnrichment getOrCreateAlterationEnrichment(
      Map<String, AlterationEnrichment> alterationEnrichmentByGene,
      AlterationCountByGene alterationCountByGene) {
    return alterationEnrichmentByGene.computeIfAbsent(
        alterationCountByGene.getHugoGeneSymbol(),
        key -> {
          AlterationEnrichment enrichment = new AlterationEnrichment();
          enrichment.setEntrezGeneId(alterationCountByGene.getEntrezGeneId());
          enrichment.setHugoGeneSymbol(alterationCountByGene.getHugoGeneSymbol());
          enrichment.setCounts(new ArrayList<>());
          return enrichment;
        });
  }

  private void addMissingCountsToAlterationEnrichment(
      AlterationEnrichment alterationEnrichment, Collection<String> groups) {
    Set<String> counts =
        alterationEnrichment.getCounts().stream()
            .map(CountSummary::getName)
            .collect(Collectors.toSet());
    if (counts.size() == groups.size()) {
      return;
    }
    Set<String> groupsWithMissingCounts = new HashSet<>(groups);
    groupsWithMissingCounts.removeAll(counts);

    for (String group : groupsWithMissingCounts) {
      CountSummary countSummary = new CountSummary();
      countSummary.setName(group);
      countSummary.setAlteredCount(0);
      countSummary.setProfiledCount(0);
      alterationEnrichment.getCounts().add(countSummary);
    }
  }

  private synchronized String getStudyIdGivenMolecularProfileId(String molecularProfileId)
      throws MolecularProfileNotFoundException {
    if (molecularProfilesMap == null) {
      molecularProfilesMap =
          alterationRepository.getAllMolecularProfiles().stream()
              .collect(
                  Collectors.toMap(
                      MolecularProfile::getStableId, molecularProfile -> molecularProfile));
    }
    var molecularProfile = molecularProfilesMap.get(molecularProfileId);
    if (molecularProfile == null) {
      log.debug("Molecular profile with id {} not found", molecularProfileId);
      throw new MolecularProfileNotFoundException(molecularProfileId);
    }
    return molecularProfile.getCancerStudyIdentifier();
  }
}
