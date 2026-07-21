package org.cbioportal.application.rest.vcolumnstore;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.cbioportal.application.rest.mapper.EmbeddingMapper;
import org.cbioportal.application.rest.response.EmbeddingDTO;
import org.cbioportal.domain.embedding.usecase.EmbeddingUseCases;
import org.cbioportal.legacy.web.config.annotation.InternalApi;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for managing and retrieving Embedding data from a column-store data source.
 *
 * <p>This controller provides an endpoint to fetch Embedding data with support for filtering the
 * response. It is designed to work with a column-store database, which is optimized for querying
 * large datasets efficiently.
 *
 * <p>Key features:
 *
 * <ul>
 *   <li>studyIds
 *   <li>entityType
 *   <li>reductionTechnique
 * </ul>
 *
 * <p>This controller is only active when the "clickhouse" profile is enabled and requires
 * appropriate read permissions for the requested cancer studies.
 *
 * <p>// * @see EmbeddingDataDTO
 */
@InternalApi
@Tag(name = "", description = " ")
@RestController
@RequestMapping("/api")
public class ColumnStoreEmbeddingController {
  // Attribute which holds the different usecases for retrieving embedding
  private final EmbeddingUseCases embeddingUseCases;

  /**
   * Constructs a new {@link ColumnStoreEmbeddingController} with the specified use case.
   *
   * @param
   */
  public ColumnStoreEmbeddingController(EmbeddingUseCases embeddingUseCases) {
    this.embeddingUseCases = embeddingUseCases;
  }

  /**
   * Fetch Mutation by exactly one sampleUniqueIdentifier or molecularProfileId must or
   * entrezGeneIds
   *
   * @param
   * @param
   * @return ResponseEntity containing list of embedding data DTOs, or empty body for META
   *     projection
   */
  @InternalApi
  @Hidden
  @PreAuthorize(
      "hasPermission(#involvedCancerStudies, 'Collection<CancerStudyId>', T(org.cbioportal.legacy.utils.security.AccessLevel).READ)")
  @RequestMapping(
      value = "/study/embeddings",
      method = RequestMethod.GET,
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<EmbeddingDTO> fetchEmbeddingInStudy(
      @Parameter(description = "Reduction technique, e.g. UMAP or PCA")
          @RequestParam(required = false)
          String reductionTechnique,
      @Parameter(description = "Entity type: patient or sample") @RequestParam(required = false)
          String entityType,
      @Parameter(description = "Cancer study identifiers to filter by.")
          @RequestParam(required = false)
          String studyId) {
    EmbeddingDTO embeddingDTO =
        EmbeddingMapper.INSTANCE.toEmbeddingDTOO(
            embeddingUseCases
                .fetchEmbeddingInStudyUseCase()
                .execute(reductionTechnique, entityType, studyId));
    return ResponseEntity.ok(embeddingDTO);
  }
}
