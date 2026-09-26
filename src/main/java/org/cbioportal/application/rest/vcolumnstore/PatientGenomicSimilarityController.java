package org.cbioportal.application.rest.vcolumnstore;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.cbioportal.application.rest.request.PatientGenomicSimilarityRequest;
import org.cbioportal.domain.mutation.GenomicSimilarityResult;
import org.cbioportal.domain.mutation.usecase.GetPatientGenomicSimilarityUseCase;
import org.cbioportal.legacy.service.exception.MolecularProfileNotFoundException;
import org.cbioportal.legacy.service.exception.PatientNotFoundException;
import org.cbioportal.legacy.web.config.annotation.InternalApi;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@InternalApi
@RestController
@RequestMapping("/api")
@Tag(name = "Patient Genomic Similarity", description = " ")
public class PatientGenomicSimilarityController {

  private final GetPatientGenomicSimilarityUseCase getPatientGenomicSimilarityUseCase;

  public PatientGenomicSimilarityController(
      GetPatientGenomicSimilarityUseCase getPatientGenomicSimilarityUseCase) {
    this.getPatientGenomicSimilarityUseCase = getPatientGenomicSimilarityUseCase;
  }

  @PreAuthorize(
      "hasPermission(#studyId, 'CancerStudyId', T(org.cbioportal.legacy.utils.security.AccessLevel).READ)")
  @RequestMapping(
      value = "/studies/{studyId}/patient-genomic-similarity",
      method = RequestMethod.POST,
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(
      description =
          "Find the patients whose mutations are most similar to those of a reference patient,"
              + " ranked by the Jaccard index of their mutated genes over the requested genes that"
              + " were profiled in both patients")
  @ApiResponse(
      responseCode = "200",
      description = "OK",
      content = @Content(schema = @Schema(implementation = GenomicSimilarityResult.class)))
  public ResponseEntity<GenomicSimilarityResult> fetchPatientGenomicSimilarity(
      @Parameter(required = true, description = "Study ID e.g. brca_tcga") @PathVariable
          String studyId,
      @Parameter(
              required = true,
              description =
                  "Mutation profile, reference patient, genes and number of patients to return")
          @Valid
          @RequestBody
          PatientGenomicSimilarityRequest request)
      throws MolecularProfileNotFoundException, PatientNotFoundException {
    return new ResponseEntity<>(
        getPatientGenomicSimilarityUseCase.execute(
            studyId,
            request.getMolecularProfileId(),
            request.getReferencePatientId(),
            request.getHugoGeneSymbols(),
            request.getTopN()),
        HttpStatus.OK);
  }
}
