package org.cbioportal.domain.mutation.usecase;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.cbioportal.domain.mutation.GenomicSimilarityResult;
import org.cbioportal.domain.mutation.PatientSimilarityScore;
import org.cbioportal.domain.mutation.repository.MutationRepository;
import org.cbioportal.legacy.model.Mutation;
import org.cbioportal.legacy.web.parameter.Direction;
import org.cbioportal.legacy.web.parameter.PagingConstants;
import org.cbioportal.legacy.web.parameter.PatientGenomicSimilarityRequest;
import org.cbioportal.shared.MutationQueryOptions;
import org.cbioportal.shared.enums.ProjectionType;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * Computes pairwise genomic similarity between a reference patient and all other patients in the
 * same mutation molecular profile, then returns the top-N most similar patients ranked by their
 * Jaccard index over a user-specified gene set.
 *
 * <h2>Algorithm</h2>
 *
 * <ol>
 *   <li>Fetch all mutations in the molecular profile, retaining only the queried genes.
 *   <li>Normalize gene symbols to uppercase and deduplicate to ensure consistent matching.
 *   <li>Build a per-patient alteration set: {@code patientId → {mutated genes}}.
 *   <li>Validate that the reference patient has at least one mutation in the queried gene panel;
 *       throw {@code 404} if the reference patient is not found in the molecular profile.
 *   <li>For each candidate patient (excluding the reference), compute the Jaccard similarity to the
 *       reference patient:
 *       <pre>
 *   Jaccard(A, B) = |A ∩ B| / |A ∪ B|
 *   </pre>
 *       where A and B are the sets of mutated genes for each patient.
 *   <li>Sort all candidates by descending similarity and return the top-N wrapped in a {@link
 *       GenomicSimilarityResult} that also exposes the reference patient's own mutation set.
 * </ol>
 *
 * <h2>Edge cases</h2>
 *
 * <ul>
 *   <li>If the reference patient has no mutations in the queried genes a {@code 404} is returned so
 *       researchers receive an actionable error rather than a confusing list of all-zero scores.
 *   <li>If a candidate patient shares no genes with the reference their Jaccard score is 0.0.
 *   <li>Gene symbols submitted in mixed case (e.g. {@code tp53}) are normalised to uppercase before
 *       comparison so callers are not sensitive to casing.
 *   <li>Duplicate gene symbols in the request are silently deduplicated.
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
   * @return a {@link GenomicSimilarityResult} containing the reference patient's mutated genes and
   *     a list of similar patients sorted by descending similarity, at most topN entries, never
   *     containing the reference patient itself
   * @throws ResponseStatusException with {@code 404 Not Found} if the reference patient has no
   *     recorded mutations in the specified molecular profile for the queried gene set
   */
  public GenomicSimilarityResult execute(PatientGenomicSimilarityRequest request) {

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

    // Normalise gene symbols: uppercase + deduplicate so "tp53" and "TP53" both match.
    Set<String> geneFilter = new HashSet<>();
    for (String symbol : request.getHugoGeneSymbols()) {
      if (symbol != null) {
        geneFilter.add(symbol.trim().toUpperCase());
      }
    }

    // Build patient → {mutated genes} map.
    Map<String, Set<String>> patientAlterations = new HashMap<>();
    for (Mutation m : mutations) {
      // Gene is populated via join in DETAILED projection; skip rows where it is absent.
      if (m.getGene() == null) {
        continue;
      }
      String symbol = m.getGene().getHugoGeneSymbol();
      if (symbol != null && geneFilter.contains(symbol.trim().toUpperCase())) {
        patientAlterations
            .computeIfAbsent(m.getPatientId(), k -> new HashSet<>())
            .add(symbol.trim().toUpperCase());
      }
    }

    // Validate: the reference patient must exist in this molecular profile.
    // Returning a list of all-zero scores to a researcher who mistyped a patient ID is
    // actively misleading; a 404 gives them an actionable error instead.
    if (!patientAlterations.containsKey(request.getReferencePatientId())) {
      throw new ResponseStatusException(
          HttpStatus.NOT_FOUND,
          "Reference patient '"
              + request.getReferencePatientId()
              + "' was not found in molecular profile '"
              + request.getMolecularProfileId()
              + "' for the requested gene panel. "
              + "Verify the patient ID and ensure the patient has mutations in at least one of the"
              + " queried genes.");
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

    List<PatientSimilarityScore> topScores = scores.stream().limit(request.getTopN()).toList();

    // Include the reference patient's own mutation set in the response.
    // This allows callers to display the context ("reference mutated X, Y, Z") alongside
    // the ranked results without needing a second API call.
    List<String> referencePatientMutatedGenes = referenceGenes.stream().sorted().toList();

    return new GenomicSimilarityResult(referencePatientMutatedGenes, topScores);
  }
}
