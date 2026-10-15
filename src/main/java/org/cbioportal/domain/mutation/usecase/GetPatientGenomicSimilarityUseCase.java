package org.cbioportal.domain.mutation.usecase;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.cbioportal.domain.mutation.GenomicSimilarityResult;
import org.cbioportal.domain.mutation.PatientGenePanel;
import org.cbioportal.domain.mutation.PatientMutatedGene;
import org.cbioportal.domain.mutation.PatientSimilarityScore;
import org.cbioportal.domain.mutation.repository.MutationRepository;
import org.cbioportal.legacy.model.GenePanelToGene;
import org.cbioportal.legacy.service.exception.MolecularProfileNotFoundException;
import org.cbioportal.legacy.service.exception.PatientNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Finds the patients of a study whose mutations are most similar to those of a reference patient.
 *
 * <p>The similarity of a patient to the reference patient is the Jaccard index of their mutated
 * genes, taken over the requested genes that were profiled in both patients: a gene that is not on
 * the gene panel of one of them cannot tell the two apart, so it is left out instead of being
 * counted as not mutated. A gene with a called mutation counts as profiled even when it is not on
 * the patient's gene panel.
 *
 * <p>Only patients with at least one mutation in the requested genes are ranked, and a patient is
 * left out when neither patient has a mutation in the genes profiled in both of them.
 */
@Service
public class GetPatientGenomicSimilarityUseCase {

  private static final Comparator<PatientSimilarityScore> MOST_SIMILAR_FIRST =
      Comparator.comparingDouble(PatientSimilarityScore::similarityScore)
          .reversed()
          .thenComparing(PatientSimilarityScore::patientId);

  private final MutationRepository mutationRepository;

  public GetPatientGenomicSimilarityUseCase(MutationRepository mutationRepository) {
    this.mutationRepository = mutationRepository;
  }

  /**
   * Finds the patients most similar to the reference patient.
   *
   * @param studyId study of the patients
   * @param molecularProfileId mutation profile of the study to compare the patients on
   * @param referencePatientId patient to compare the other patients with
   * @param hugoGeneSymbols genes to compare the patients on, matched case-insensitively
   * @param topN maximum number of similar patients to return
   * @return the requested genes mutated in the reference patient, and at most {@code topN} other
   *     patients ordered from most to least similar (ties by patient ID)
   * @throws MolecularProfileNotFoundException if the study has no mutation profile with this ID
   * @throws PatientNotFoundException if the reference patient was not profiled in the molecular
   *     profile
   */
  public GenomicSimilarityResult execute(
      String studyId,
      String molecularProfileId,
      String referencePatientId,
      Collection<String> hugoGeneSymbols,
      int topN)
      throws MolecularProfileNotFoundException, PatientNotFoundException {
    if (!mutationRepository.isMutationMolecularProfileOfStudy(studyId, molecularProfileId)) {
      throw new MolecularProfileNotFoundException(molecularProfileId);
    }
    String referencePatient = referencePatientId.trim();
    List<String> genes =
        hugoGeneSymbols.stream()
            .map(gene -> gene.trim().toUpperCase(Locale.ROOT))
            .distinct()
            .sorted()
            .toList();

    Map<String, Set<String>> mutatedGenesByPatient =
        groupBy(
            mutationRepository.getMutatedGenesOfPatients(molecularProfileId, genes),
            PatientMutatedGene::patientId,
            PatientMutatedGene::hugoGeneSymbol);
    Map<String, Set<String>> genePanelsByPatient =
        groupBy(
            mutationRepository.getGenePanelsOfPatients(
                studyId, molecularProfileId, genes, referencePatient),
            PatientGenePanel::patientId,
            PatientGenePanel::genePanelId);
    if (!genePanelsByPatient.containsKey(referencePatient)) {
      throw new PatientNotFoundException(studyId, referencePatient);
    }

    Set<String> referenceMutatedGenes =
        mutatedGenesByPatient.getOrDefault(referencePatient, Set.of());
    List<String> sortedReferenceMutatedGenes = referenceMutatedGenes.stream().sorted().toList();
    if (referenceMutatedGenes.isEmpty()) {
      // the similarity to every patient would be 0, so there is nothing to rank
      return new GenomicSimilarityResult(sortedReferenceMutatedGenes, List.of());
    }

    Map<String, Set<String>> profiledGenesByPatient =
        getProfiledGenesByPatient(genes, mutatedGenesByPatient, genePanelsByPatient);
    Set<String> referenceProfiledGenes = profiledGenesByPatient.get(referencePatient);

    List<PatientSimilarityScore> scores = new ArrayList<>();
    mutatedGenesByPatient.forEach(
        (patientId, mutatedGenes) -> {
          if (patientId.equals(referencePatient)) {
            return;
          }
          Set<String> comparedGenes =
              intersection(referenceProfiledGenes, profiledGenesByPatient.get(patientId));
          Set<String> referenceMutated = intersection(referenceMutatedGenes, comparedGenes);
          Set<String> patientMutated = intersection(mutatedGenes, comparedGenes);

          Set<String> mutatedInEither = new HashSet<>(referenceMutated);
          mutatedInEither.addAll(patientMutated);
          if (mutatedInEither.isEmpty()) {
            return;
          }
          Set<String> mutatedInBoth = intersection(referenceMutated, patientMutated);
          scores.add(
              new PatientSimilarityScore(
                  patientId,
                  (double) mutatedInBoth.size() / mutatedInEither.size(),
                  mutatedInBoth.stream().sorted().toList()));
        });

    return new GenomicSimilarityResult(
        sortedReferenceMutatedGenes,
        scores.stream().sorted(MOST_SIMILAR_FIRST).limit(topN).toList());
  }

  /**
   * Gets, for every patient, the requested genes that are on one of the patient's gene panels or
   * that have a called mutation in the patient.
   */
  private Map<String, Set<String>> getProfiledGenesByPatient(
      List<String> genes,
      Map<String, Set<String>> mutatedGenesByPatient,
      Map<String, Set<String>> genePanelsByPatient) {
    Set<String> genePanelIds =
        genePanelsByPatient.values().stream().flatMap(Set::stream).collect(Collectors.toSet());
    Map<String, Set<String>> genesByGenePanel =
        groupBy(
            mutationRepository.getGenePanelGenes(genePanelIds, genes),
            GenePanelToGene::getGenePanelId,
            GenePanelToGene::getHugoGeneSymbol);

    Map<String, Set<String>> profiledGenesByPatient = new HashMap<>();
    genePanelsByPatient.forEach(
        (patientId, genePanels) -> {
          Set<String> profiledGenes =
              profiledGenesByPatient.computeIfAbsent(patientId, id -> new HashSet<>());
          genePanels.forEach(
              genePanel ->
                  profiledGenes.addAll(genesByGenePanel.getOrDefault(genePanel, Set.of())));
        });
    mutatedGenesByPatient.forEach(
        (patientId, mutatedGenes) ->
            profiledGenesByPatient
                .computeIfAbsent(patientId, id -> new HashSet<>())
                .addAll(mutatedGenes));
    return profiledGenesByPatient;
  }

  private static <T> Map<String, Set<String>> groupBy(
      Collection<T> rows, Function<T, String> key, Function<T, String> value) {
    return rows.stream()
        .collect(Collectors.groupingBy(key, Collectors.mapping(value, Collectors.toSet())));
  }

  private static Set<String> intersection(Set<String> first, Set<String> second) {
    Set<String> intersection = new HashSet<>(first);
    intersection.retainAll(second);
    return intersection;
  }
}
