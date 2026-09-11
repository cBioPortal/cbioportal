package org.cbioportal.legacy.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.cbioportal.domain.mutation.GenomicSimilarityResult;
import org.cbioportal.domain.mutation.usecase.GetPatientGenomicSimilarityUseCase;
import org.cbioportal.legacy.web.parameter.PatientGenomicSimilarityRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST endpoint for patient genomic similarity search.
 *
 * <p>Given a reference patient and a set of genes, returns the top-N patients in the same study
 * whose mutation profile is most similar to the reference patient, ranked by Jaccard similarity.
 * This enables researchers to rapidly identify historical patients with comparable genomic profiles
 * without exporting and post-processing data locally.
 *
 * <p>The response includes the reference patient's own mutated genes (over the queried panel) so
 * the caller can display full context alongside the ranked results without issuing a second
 * request.
 *
 * <p>Gene symbols in the request are normalised to uppercase before comparison so the caller is not
 * sensitive to casing (e.g. {@code tp53} is treated identically to {@code TP53}).
 */
@RestController
@Validated
@RequestMapping("/api/studies/{studyId}")
@Tag(name = "Patient Genomic Similarity", description = "Find genomically similar patients")
public class PatientGenomicSimilarityController {

  private final GetPatientGenomicSimilarityUseCase getPatientGenomicSimilarityUseCase;

  public PatientGenomicSimilarityController(
      GetPatientGenomicSimilarityUseCase getPatientGenomicSimilarityUseCase) {
    this.getPatientGenomicSimilarityUseCase = getPatientGenomicSimilarityUseCase;
  }

  /**
   * Returns the top-N patients most genomically similar to a reference patient, ranked by Jaccard
   * similarity over a user-specified mutation gene set.
   *
   * <p>Similarity is computed as:
   *
   * <pre>
   *   Jaccard(reference, candidate) = |mutated_genes(reference) ∩ mutated_genes(candidate)|
   *                                 / |mutated_genes(reference) ∪ mutated_genes(candidate)|
   * </pre>
   *
   * over the genes listed in the request body. A score of 1.0 means identical mutation profiles
   * across all queried genes; 0.0 means no mutations in common.
   *
   * @param studyId the cancer study whose patients are searched
   * @param request specifies the molecular profile, reference patient, gene set, and topN
   * @return full similarity result including the reference patient's mutations and ranked
   *     candidates
   */
  @PostMapping(
      value = "/patient-genomic-similarity",
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize(
      "hasPermission(#studyId, 'CancerStudyId',"
          + " T(org.cbioportal.legacy.utils.security.AccessLevel).READ)")
  @Operation(
      summary = "Find genomically similar patients",
      description =
          "Returns the top-N patients in the study whose mutation profile is most similar to the"
              + " reference patient, ranked by Jaccard similarity over the specified gene set."
              + " Gene symbols are normalised to uppercase before comparison."
              + " The response also includes the reference patient's own mutated genes within the"
              + " queried panel so no second API call is needed to render full context.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Successful operation",
        content = @Content(schema = @Schema(implementation = GenomicSimilarityResult.class))),
    @ApiResponse(
        responseCode = "400",
        description =
            "Invalid request — e.g. blank molecularProfileId, empty gene list, topN out of range",
        content = @Content),
    @ApiResponse(
        responseCode = "404",
        description =
            "Reference patient not found in the specified molecular profile for the queried gene"
                + " panel. Verify the patient ID and confirm the patient has at least one mutation"
                + " in the requested genes.",
        content = @Content),
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
  })
  public ResponseEntity<GenomicSimilarityResult> getPatientGenomicSimilarity(
      @Parameter(description = "Study ID", required = true) @PathVariable String studyId,
      @Valid @RequestBody PatientGenomicSimilarityRequest request) {

    GenomicSimilarityResult result = getPatientGenomicSimilarityUseCase.execute(studyId, request);
    return ResponseEntity.ok(result);
  }
}
