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
import org.cbioportal.legacy.model.MolecularProfile;
import org.cbioportal.legacy.model.Mutation;
import org.cbioportal.legacy.service.MolecularProfileService;
import org.cbioportal.legacy.service.exception.MolecularProfileNotFoundException;
import org.cbioportal.legacy.web.parameter.Direction;
import org.cbioportal.legacy.web.parameter.PagingConstants;
import org.cbioportal.legacy.web.parameter.PatientGenomicSimilarityRequest;
import org.cbioportal.shared.MutationQueryOptions;
import org.cbioportal.shared.enums.ProjectionType;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * Computes pairwise genomic similarity between a reference patient and all other patients in the
 * same mutation molecular profile, then returns the top-N most similar patients ranked by their
 * Jaccard index over a user-specified gene set.
 *
 * <h2>Algorithm</h2>
 *
 * <ol>
 *   <li>Validate that the requested molecular profile exists and belongs to the given study.
 *   <li>Fetch all mutations in the molecular profile. {@code PagingConstants.MAX_PAGE_SIZE}
 *       (10,000,000) comfortably exceeds the largest known mutation dataset; if a future study
 *       approaches this limit callers will need server-side gene-level filtering at the DB layer.
 *   <li>Normalise gene symbols to uppercase and deduplicate.
 *   <li>Build a per-patient alteration set: {@code patientId → {mutated genes}}.
 *   <li>Guard against runaway in-memory computation: studies with more than {@value
 *       #MAX_PATIENTS_FOR_SIMILARITY} distinct patients in the profile are rejected with 400.
 *   <li>Validate that the reference patient is present.
 *   <li>Compute Jaccard similarity for every candidate and return the top-N wrapped in a {@link
 *       GenomicSimilarityResult}.
 * </ol>
 *
 * <h2>Edge cases</h2>
 *
 * <ul>
 *   <li>Reference patient not in profile → HTTP 404 with actionable message.
 *   <li>Profile not in study → HTTP 400.
 *   <li>Study exceeds patient cap → HTTP 400.
 *   <li>Gene symbols in mixed case or duplicated → silently normalised / deduplicated.
 *   <li>Zero intersection → Jaccard score of 0.0.
 * </ul>
 *
 * <h2>Rate limiting</h2>
 *
 * <p>This endpoint performs an in-memory O(P × G) computation where P is the number of patients and
 * G is the gene-panel size. No per-user rate limiting is applied at the application layer;
 * operators should configure a reverse proxy (e.g. nginx {@code limit_req}) for high-traffic
 * deployments.
 */
@Service
public class GetPatientGenomicSimilarityUseCase {

  /**
   * Maximum number of distinct patients in a molecular profile that we will process. Studies above
   * this threshold are rejected with HTTP 400 to prevent runaway in-memory Jaccard computation.
   * Raise this value — or introduce streaming / pre-computation — when supporting very large
   * pan-cancer cohorts.
   */
  static final int MAX_PATIENTS_FOR_SIMILARITY = 50_000;

  private final MutationRepository mutationRepository;
  private final MolecularProfileService molecularProfileService;

  public GetPatientGenomicSimilarityUseCase(
      MutationRepository mutationRepository, MolecularProfileService molecularProfileService) {
    this.mutationRepository = mutationRepository;
    this.molecularProfileService = molecularProfileService;
  }

  /**
   * Finds the top-N patients most genomically similar to the reference patient.
   *
   * @param studyId the cancer study ID from the URL path; used to verify the molecular profile
   *     belongs to this study
   * @param request the similarity request specifying molecular profile, reference patient, genes,
   *     and topN
   * @return a {@link GenomicSimilarityResult} containing the reference patient's mutated genes and
   *     a list of similar patients sorted by descending similarity, at most topN entries, never
   *     containing the reference patient itself
   * @throws ResponseStatusException with {@code 400 Bad Request} if the molecular profile does not
   *     belong to the given study, or if the study exceeds the patient cap
   * @throws ResponseStatusException with {@code 404 Not Found} if the reference patient has no
   *     recorded mutations in the specified molecular profile for the queried gene set
   */
  @Transactional(readOnly = true)
  public GenomicSimilarityResult execute(String studyId, PatientGenomicSimilarityRequest request) {

    // ── Fix #2: validate that the molecular profile belongs to the requested study ──────────
    // Without this check a caller can pass studyId=brca_tcga but molecularProfileId=luad_mutations
    // and bypass the intent of the @PreAuthorize guard on the controller.
    MolecularProfile profile;
    try {
      profile = molecularProfileService.getMolecularProfile(request.getMolecularProfileId());
    } catch (MolecularProfileNotFoundException ex) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST,
          "Molecular profile '" + request.getMolecularProfileId() + "' does not exist.");
    }

    if (!studyId.equals(profile.getCancerStudyIdentifier())) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST,
          "Molecular profile '"
              + request.getMolecularProfileId()
              + "' does not belong to study '"
              + studyId
              + "'. It belongs to '"
              + profile.getCancerStudyIdentifier()
              + "'.");
    }

    // Fetch all mutations in the molecular profile without pagination. DETAILED projection
    // is required so the nested Gene object (and thus getHugoGeneSymbol()) is populated
    // by the MyBatis result-map join.
    // NOTE: PagingConstants.MAX_PAGE_SIZE = 10,000,000. This comfortably covers every known
    // study. A future improvement should pass resolved Entrez IDs so the DB filters at the
    // SQL level, reducing the data transfer by up to 99% for small gene panels.
    List<Mutation> mutations =
        mutationRepository.getMutationsInMultipleMolecularProfiles(
            List.of(request.getMolecularProfileId()),
            null, // all samples
            null, // all genes — filtered in Java below; see NOTE above
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

    // ── Fix #5: guard against runaway in-memory computation on very large studies ──────────
    if (patientAlterations.size() > MAX_PATIENTS_FOR_SIMILARITY) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST,
          "Study '"
              + studyId
              + "' contains "
              + patientAlterations.size()
              + " patients with mutations in the queried gene panel, which exceeds the"
              + " per-request limit of "
              + MAX_PATIENTS_FOR_SIMILARITY
              + ". Use a more focused gene panel or contact the cBioPortal team for"
              + " pre-computed similarity support.");
    }

    // Trim the reference patient ID to avoid false 404s caused by trailing whitespace
    // that can be introduced by some data importers.
    String referencePatientId = request.getReferencePatientId().trim();

    // ── Fix #2 (cont.): validate that reference patient exists in this profile ──────────────
    if (!patientAlterations.containsKey(referencePatientId)) {
      throw new ResponseStatusException(
          HttpStatus.NOT_FOUND,
          "Reference patient '"
              + referencePatientId
              + "' was not found in molecular profile '"
              + request.getMolecularProfileId()
              + "' for the requested gene panel. "
              + "Verify the patient ID and ensure the patient has mutations in at least one of the"
              + " queried genes.");
    }

    Set<String> referenceGenes = patientAlterations.getOrDefault(referencePatientId, Set.of());

    // Compute Jaccard similarity for every patient except the reference.
    List<PatientSimilarityScore> scores = new ArrayList<>();
    for (Map.Entry<String, Set<String>> entry : patientAlterations.entrySet()) {
      if (entry.getKey().equals(referencePatientId)) {
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

    // Include the reference patient's own mutation set so callers can display context
    // alongside the ranked results without needing a second API call.
    List<String> referencePatientMutatedGenes = referenceGenes.stream().sorted().toList();

    return new GenomicSimilarityResult(referencePatientMutatedGenes, topScores);
  }
}
