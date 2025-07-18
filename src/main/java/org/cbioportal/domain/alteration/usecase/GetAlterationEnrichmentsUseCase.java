package org.cbioportal.domain.alteration.usecase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
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
import org.cbioportal.legacy.model.GenePanelToGene;
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

    // TODO: this should be filtered by the alteration filter
    // e.g. if we are not looking for mutations, we should not take into account panels that belong
    // to mutation profiles

    // you will get multiple sample to panel mappings for each sample
    List<SampleToPanel> sampleToGenePanels =
        alterationRepository.getSampleToGenePanels(sampleStableIds, enrichmentType);

    // group the panels by the entit ids which they are associated with
    // this tells us for each entity, what gene panels were applied
    // for example a single sample might have a mutation panel and a cna panel applied to it
    // the panel could be the same or different, as panels are not specific to an alteration type

    // Note: that once we heve the panels in a list, we lose the alteration type context,
    // but this is ok because we're looking at the basket of panels which were assayed
    // irrespective of the alteration type. in other words, in the comparison view
    // we regard a sample as profiled for a given gene if ANY of the associated panels included it
    // this could potentially mislead the user because they might think that a sample
    // was profiled for mutations in a certain gene, when in fact, it was not. That's
    // a limitation of the current design

    Map<String, Set<String>> entityToPanelMap =
        sampleToGenePanels.stream()
            .collect(
                Collectors.groupingBy(
                    SampleToPanel::getSampleUniqueId,
                    Collectors.mapping(e -> e.getGenePanelId(), Collectors.toSet())));

    // Many of the samples are governed by the same combination of panels and therefor,
    // we only need to count them once.
    // We want to group the samples by a key that represents the set of panels applied to them
    // For any gene which is profiled by those panels, the count of samples will always be the same

    // the panelCombinationToEntityList are the set of samples which are governed by the same set of
    // panels
    // the key is the concatenation of panel ids, the value is list of entities

    // Could the entity appear in multiple panelCombinationToEntityList?
    // No, a sample will only ever appear in one clump and that is how we avoid double counting them
    // We can thus add these clump totals later without fear of double counting the samples
    Map<String, List<String>> panelCombinationToEntityList =
        entityToPanelMap.keySet().stream()
            // the keyset are the entity id
            // we are grouping the entity ids by the panelCombinations
            .collect(
                Collectors.groupingBy(
                    sampleId ->
                        entityToPanelMap.get(sampleId).stream().collect(Collectors.joining(","))));

    List<String> molecularProfileIdsList =
        new ArrayList<>(caseIdsAndMolecularProfileIds.getSecond());
    List<String> sampleStableIdsList = new ArrayList<>(caseIdsAndMolecularProfileIds.getFirst());

    // now get the count of altered entities by gene
    List<AlterationCountByGene> alterationCounts =
        enrichmentType.equals(EnrichmentType.SAMPLE)
            ? alterationRepository.getAlterationCountByGeneGivenSamplesAndMolecularProfiles(
                sampleStableIdsList, molecularProfileIdsList, alterationFilter)
            : alterationRepository.getAlterationCountByGeneGivenPatientsAndMolecularProfiles(
                sampleStableIdsList, molecularProfileIdsList, alterationFilter);

    HashMap<String, AlterationCountByGene> alteredGenesWithCounts = new HashMap();

    // populate alteredGenesWithCounts
    // why do we need to tally here? can a gene appear multiple
    // times in alterationCounts? apparently so.  why would that be?
    // i doesn't seem it's possible to get multiple entries for the same gene in alterationCounts
    // so we may not need to do this at all.
    // TODO: refactor when tests are place
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

    // a panelCombinationToEntityList is a group of samples that are governed by the same set of
    // panels
    // for each gene, we are going to add up the panelCombinationToEntityList whose panels include
    // the gene
    panelCombinationToEntityList.entrySet().stream()
        .forEach(
            entry -> {
              // for all panels in each clump, we need to get the associated list of genes
              // the Map key is the panel id and the value is the list of genes it covers
              List<Map<String, GenePanelToGene>> geneLists =
                  // the panelCombinationToEntityList key is the concatenated list of panel ids
                  // (TODO, use a composite key instead of a string)
                  // for each panel id, we get the list of genes it covers
                  // and now we have the total list of genes which are covered by any panel in the
                  // combinatation
                  Arrays.stream(entry.getKey().split(","))
                      .map(panelId -> panelToGeneMap.get(panelId))
                      .collect(Collectors.toList());

              // it's counterintuitive, but we want the union of the genes in the panels
              // because if a gene is in ANY of the panels, it will be counted as profiled
              Set<GenePanelToGene> mergeGenes =
                  geneLists.stream()
                      .flatMap(map -> map.values().stream())
                      .collect(
                          Collectors.toMap(
                              GenePanelToGene::getHugoGeneSymbol,
                              gene -> gene,
                              (existing, replacement) -> existing))
                      .values()
                      .stream()
                      .collect(Collectors.toSet());

              // we know that each of the genes in merged genes are covered by the panels in the
              // clump
              // and therefor we can add the count of the clump's samples to the profiled count for
              // each gene
              // again, we know we aren't double counting samples because a sample can only appear
              // in a single clump
              mergeGenes.stream()
                  .forEach(
                      gene -> {
                        String hugoGeneSymbol = gene.getHugoGeneSymbol();
                        if (geneCount.containsKey(hugoGeneSymbol)) {
                          var count = geneCount.get(hugoGeneSymbol);
                          count.setNumberOfProfiledCases(
                              count.getNumberOfProfiledCases() + entry.getValue().size());
                        } else {
                          var alterationCountByGene = new AlterationCountByGene();
                          alterationCountByGene.setHugoGeneSymbol(hugoGeneSymbol);
                          alterationCountByGene.setEntrezGeneId(gene.getEntrezGeneId());
                          alterationCountByGene.setNumberOfProfiledCases(entry.getValue().size());
                          alterationCountByGene.setNumberOfAlteredCases(0);
                          geneCount.put(hugoGeneSymbol, alterationCountByGene);
                        }
                      });
            });

    // whey can't we do this above when we are populating the altered count?
    // TODO: refactor when tests are in place
    geneCount.entrySet().stream()
        .forEach(
            n -> {
              if (alteredGenesWithCounts.containsKey(n.getKey())) {
                // populated number of altered cases
                n.getValue()
                    .setNumberOfAlteredCases(
                        alteredGenesWithCounts.get(n.getKey()).getNumberOfAlteredCases());

                // set entrez gene id because we didn't have it at our disposal above
                n.getValue()
                    .setEntrezGeneId(alteredGenesWithCounts.get(n.getKey()).getEntrezGeneId());
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
