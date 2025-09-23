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

    // calculate the alteration count by gene for each group in parallel
    // for performance reasons, we defer calculating the profiled counts until a later step
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
              // groups which do not have any alterations for a given gene will not have an entry in
              // the
              // counts array. We need to add those entries with a count of zero.
              addMissingCountsToAlterationEnrichment(alterationEnrichment, groups);

              // calculate the p-value
              var pValue =
                  AlterationEnrichmentScoreUtil.calculateEnrichmentScore(alterationEnrichment);
              alterationEnrichment.setpValue(pValue);
              return alterationEnrichment;
            })
        .collect(Collectors.toSet());
  }

  private Pair<String, List<AlterationCountByGene>> fetchAlterationCountByGeneByGroup(
      String group,
      List<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers,
      EnrichmentType enrichmentType,
      AlterationFilter alterationFilter)
      throws MolecularProfileNotFoundException {
    // entities can be either samples or patients depending on the enrichment type
    Pair<Set<String>, Set<String>> entityIdsAndMolecularProfileIds =
        this.extractCaseIdsAndMolecularProfiles(molecularProfileCaseIdentifiers);

    // an earlier implementation counted the number of entities profiled for each gene
    // this was redundant because the number of entities profiled is the same for gene which is
    // covered by a given combination of profiles
    // we can thus count the number of entities profiled for each gene panel combination and refer
    // to
    // that for every gene
    // which is covered by the panels in the combination

    Map<String, List<String>> panelCombinationToEntityList =
        buildPanelCombinationToEntityMapping(
            entityIdsAndMolecularProfileIds.getFirst(),
            entityIdsAndMolecularProfileIds.getSecond(),
            enrichmentType);

    HashMap<String, AlterationCountByGene> alteredGenesWithCounts =
        processAlterationCounts(entityIdsAndMolecularProfileIds, enrichmentType, alterationFilter);

    // we need a map of panels to genes which are profiled by them
    var panelToGeneMap = alterationRepository.getGenePanelsToGenes();

    Map<String, AlterationCountByGene> geneCount =
        calculateProfiledCasesPerGene(panelCombinationToEntityList, panelToGeneMap);

    mergeAlteredCountsWithProfiledCounts(geneCount, alteredGenesWithCounts);

    return Pair.of(group, geneCount.values().stream().toList());
  }

  private Map<String, List<String>> buildPanelCombinationToEntityMapping(
      Set<String> entityStableIds, Set<String> profileIds, EnrichmentType enrichmentType) {
    List<String> entityStableIdsList = new ArrayList<>(entityStableIds);

    // you will get multiple entities to panel mappings for each entities
    List<SampleToPanel> entityToGenePanels =
        alterationRepository.getEntityToGenePanels(
            entityStableIdsList, new ArrayList<>(profileIds), enrichmentType);

    // group the panels by the entity ids which they are associated with
    // this tells us for each entity, what gene panels were applied
    // for example a single entities might have a mutation panel and a cna panel applied to it
    // the panel could be the same or different, as panels are not specific to an alteration type

    // Note: that once we heve the panels in a list, we lose the alteration type context,
    // but this is ok because we're looking at the basket of panels which were assayed
    // irrespective of the alteration type. in other words, in the comparison view
    // we regard a entities as profiled for a given gene if ANY of the associated panels included it
    // this could potentially mislead the user because they might think that a entities
    // was profiled for mutations in a certain gene, when in fact, it was not. That's
    // a limitation of the current design

    Map<String, Set<String>> entityToPanelMap =
        entityToGenePanels.stream()
            .collect(
                Collectors.groupingBy(
                    SampleToPanel::getSampleUniqueId,
                    Collectors.mapping(e -> e.getGenePanelId(), Collectors.toSet())));

    // Many of the entities are governed by the same combination of panels and therefor,
    // we only need to count them once.
    // We want to group the entities by a key that represents the set of panels applied to them
    // For any gene which is profiled by those panels, the count of entities will always be the same

    // the panelCombinationToEntityList are the set of entities which are governed by the same set
    // of
    // panels
    // the key is the concatenation of panel ids, the value is list of entities

    // Could the entity appear in multiple panelCombinationToEntityList?
    // No, a entities will only ever appear in one clump and that is how we avoid double counting
    // them
    // We can thus add these clump totals later without fear of double counting the entities
    return entityToPanelMap.keySet().stream()
        // the keyset are the entity id
        // we are grouping the entity ids by the panelCombinations
        .collect(
            Collectors.groupingBy(
                entityId ->
                    entityToPanelMap.get(entityId).stream().collect(Collectors.joining(","))));
  }

  private HashMap<String, AlterationCountByGene> processAlterationCounts(
      Pair<Set<String>, Set<String>> caseIdsAndMolecularProfileIds,
      EnrichmentType enrichmentType,
      AlterationFilter alterationFilter) {
    List<String> molecularProfileIdsList =
        new ArrayList<>(caseIdsAndMolecularProfileIds.getSecond());
    List<String> entityStableIdsList = new ArrayList<>(caseIdsAndMolecularProfileIds.getFirst());

    // now get the count of altered entities by gene
    List<AlterationCountByGene> alterationCounts =
        enrichmentType.equals(EnrichmentType.SAMPLE)
            ? alterationRepository.getAlterationCountByGeneGivenSamplesAndMolecularProfiles(
                entityStableIdsList, molecularProfileIdsList, alterationFilter)
            : alterationRepository.getAlterationCountByGeneGivenPatientsAndMolecularProfiles(
                entityStableIdsList, molecularProfileIdsList, alterationFilter);

    HashMap<String, AlterationCountByGene> alteredGenesWithCounts = new HashMap<>();

    // populate alteredGenesWithCounts
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

    return alteredGenesWithCounts;
  }

  private Map<String, AlterationCountByGene> calculateProfiledCasesPerGene(
      Map<String, List<String>> panelCombinationToEntityList,
      Map<String, Map<String, GenePanelToGene>> panelToGeneMap) {

    var geneCount = new HashMap<String, AlterationCountByGene>();

    // a panelCombinationToEntityList is a group of entities that are governed by the same set of
    // panels
    // for each gene, we are going to add up the panelCombinationToEntityList whose panels include
    // the gene
    panelCombinationToEntityList.entrySet().stream()
        .forEach(
            entry -> {
              // for all panels in each clump, we need to get the associated list of genes
              // the Map key is the panel id and the value is the list of genes it covers
              List<Map<String, GenePanelToGene>> geneLists =
                  // The panelCombinationToEntityList key is the concatenated list of panel ids
                  // for each panel id, we get the list of genes it covers
                  // and now we have the total list of genes which are covered by any panel in the
                  Arrays.stream(entry.getKey().split(","))
                      .map(panelId -> panelToGeneMap.get(panelId))
                      .collect(Collectors.toList());

              // It's counterintuitive, but we want the union of the genes in the panels
              // because if a gene is in ANY of the panels, it will be counted as profiled
              // this may confuse a user into thinking that a entity was profiled for a given
              // alteration type when in fact it wasn't
              // but that's a limitation of the current design
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

              // We know that each of the genes in merged genes are covered by the panels in combo
              // and therefor we can add the count of the combo entities to the profiled count for
              // each gene
              // We know we aren't double counting entities because an entity can only appear
              // in a single combo
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

    return geneCount;
  }

  private void mergeAlteredCountsWithProfiledCounts(
      Map<String, AlterationCountByGene> geneCount,
      HashMap<String, AlterationCountByGene> alteredGenesWithCounts) {
    geneCount.entrySet().stream()
        .forEach(
            n -> {
              if (alteredGenesWithCounts.containsKey(n.getKey())) {
                // populate number of altered cases
                n.getValue()
                    .setNumberOfAlteredCases(
                        alteredGenesWithCounts.get(n.getKey()).getNumberOfAlteredCases());

                // set entrez gene id because we didn't have it at our disposal earlier
                n.getValue()
                    .setEntrezGeneId(alteredGenesWithCounts.get(n.getKey()).getEntrezGeneId());
              }
            });
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
