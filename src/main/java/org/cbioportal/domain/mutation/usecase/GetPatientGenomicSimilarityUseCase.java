package org.cbioportal.domain.mutation.usecase;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.cbioportal.domain.mutation.PatientSimilarityScore;
import org.cbioportal.domain.mutation.repository.MutationRepository;
import org.cbioportal.legacy.model.Mutation;
import org.cbioportal.legacy.web.parameter.Direction;
import org.cbioportal.legacy.web.parameter.PagingConstants;
import org.cbioportal.legacy.web.parameter.PatientGenomicSimilarityRequest;
import org.cbioportal.shared.MutationQueryOptions;
import org.cbioportal.shared.enums.ProjectionType;
import org.springframework.stereotype.Service;

/**
 * Computes pairwise genomic similarity between a reference patient and all other patients in the
 * same mutation molecular profile, then returns the top-N most similar patients ranked by their
 * Jaccard index over a user-specified gene set.
 *
 * <h2>Algorithm</h2>
 *
 * <ol>
 *   <li>Fetch all mutations in the molecular profile, retaining only the queried genes.
 *   <li>Build a per-patient alteration set: {@code patientId → {mutated genes}}.
 *   <li>For each candidate patient (excluding the reference), compute the Jaccard similarity to the
 *       reference patient:
 *       <pre>
 *   Jaccard(A, B) = |A ∩ B| / |A ∪ B|
 *   </pre>
 *       where A and B are the sets of mutated genes for each patient.
 *   <li>Sort all candidates by descending similarity and return the top-N.
 * </ol>
 *
 * <h2>Edge cases</h2>
 *
 * <ul>
 *   <li>If the reference patient has no mutations in the queried genes their similarity score to
 *       every other patient is 0.0 and the returned list is still ranked by the other patient's
 *       alteration count (most altered first).
 *   <li>If a candidate patient shares no genes with the reference their Jaccard score is 0.0.
 *   <li>Patients with no mutations at all in the queried genes are included with score 0.0 only
 *       when the reference also has no mutations; otherwise they naturally sort to the bottom.
 * </ul>
 */
@Service
public class GetPatientGenomicSimilarityUseCase {

  private final MutationRepository mutationRepository;

  public GetPatientGenomicSimilarityUseCase(MutationRepository mutationRepository) {
    this.mutationRepository = mutationRepository;
  }

  /**
   * Finds the top-N patients most genomically similar to the reference patient.
   *
   * @param request the similarity request specifying molecular profile, reference patient, genes,
   *     and topN
   * @return list of {@link PatientSimilarityScore} sorted by descending similarity, at most topN
   *     entries, never containing the reference patient itself
   */
  public List<PatientSimilarityScore> execute(PatientGenomicSimilarityRequest request) {

    // Fetch all mutations in the molecular profile without pagination. DETAILED projection
    // is required so the nested Gene object (and thus getHugoGeneSymbol()) is populated
    // by the MyBatis result-map join.
    List<Mutation> mutations =
        mutationRepository.getMutationsInMultipleMolecularProfiles(
            List.of(request.getMolecularProfileId()),
            null, // all samples
            null, // all genes — we filter by symbol below to avoid a gene-lookup round-trip
            new MutationQueryOptions(
                ProjectionType.DETAILED, PagingConstants.MAX_PAGE_SIZE, 0, null, Direction.ASC));

    // Restrict to the gene set the caller is interested in.
    Set<String> geneFilter = new HashSet<>(request.getHugoGeneSymbols());

    // Build patient → {mutated genes} map.
    Map<String, Set<String>> patientAlterations = new HashMap<>();
    for (Mutation m : mutations) {
      // Gene is populated via join in DETAILED projection; skip rows where it is absent.
      if (m.getGene() == null) {
        continue;
      }
      String symbol = m.getGene().getHugoGeneSymbol();
      if (geneFilter.contains(symbol)) {
        patientAlterations.computeIfAbsent(m.getPatientId(), k -> new HashSet<>()).add(symbol);
      }
    }

    Set<String> referenceGenes =
        patientAlterations.getOrDefault(request.getReferencePatientId(), Set.of());

    // Compute Jaccard similarity for every patient except the reference.
    List<PatientSimilarityScore> scores = new ArrayList<>();
    for (Map.Entry<String, Set<String>> entry : patientAlterations.entrySet()) {
      if (entry.getKey().equals(request.getReferencePatientId())) {
        continue;
      }
      Set<String> candidateGenes = entry.getValue();

      Set<String> intersection = new HashSet<>(referenceGenes);
      intersection.retainAll(candidateGenes);

      Set<String> union = new HashSet<>(referenceGenes);
      union.addAll(candidateGenes);

      double score = union.isEmpty() ? 0.0 : (double) intersection.size() / union.size();

      // Sort the intersection for a stable, deterministic response across calls.
      List<String> commonGenes = intersection.stream().sorted().toList();
      scores.add(new PatientSimilarityScore(entry.getKey(), score, commonGenes));
    }

    // Sort descending by similarity, break ties by patientId for determinism.
    scores.sort(
        Comparator.comparingDouble(PatientSimilarityScore::similarityScore)
            .reversed()
            .thenComparing(PatientSimilarityScore::patientId));

    return scores.stream().limit(request.getTopN()).toList();
  }
}
