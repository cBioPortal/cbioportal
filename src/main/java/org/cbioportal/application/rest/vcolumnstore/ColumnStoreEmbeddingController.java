package org.cbioportal.application.rest.vcolumnstore;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.cbioportal.application.rest.mapper.EmbeddingMapper;
import org.cbioportal.application.rest.response.EmbeddingDTO;
import org.cbioportal.domain.embedding.usecase.EmbeddingUseCases;
import org.cbioportal.legacy.web.config.InternalApiTags;
import org.cbioportal.legacy.web.config.annotation.InternalApi;
import org.cbioportal.legacy.web.parameter.EmbeddingFilter;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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
 * @see EmbeddingDTO
 */
@InternalApi
@Tag(name = InternalApiTags.EMBEDDING, description = "Embedding data")
@RestController
@RequestMapping("/api")
public class ColumnStoreEmbeddingController {

  private final EmbeddingUseCases embeddingUseCases;

  /**
   * Constructs a new {@link ColumnStoreEmbeddingController} with the specified use case.
   *
   * @param embeddingUseCases the use cases used to fetch embedding data for a study
   */
  public ColumnStoreEmbeddingController(EmbeddingUseCases embeddingUseCases) {
    this.embeddingUseCases = embeddingUseCases;
  }

  /**
   * Fetches embedding (dimensionality-reduction) coordinates for one or more studies, optionally
   * filtered by reduction technique (e.g. "umap", "pca") and entity type (e.g. "PATIENT",
   * "SAMPLE").
   *
   * @param embeddingFilter the study IDs and optional reduction technique / entity type filters
   * @return a {@link ResponseEntity} wrapping the {@link EmbeddingDTO} for the requested studies
   */
  @PreAuthorize(
      "hasPermission(#involvedCancerStudies, 'Collection<CancerStudyId>', T(org.cbioportal.legacy.utils.security.AccessLevel).READ)")
  @RequestMapping(
      value = "/study/embeddings",
      method = RequestMethod.POST,
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<EmbeddingDTO> fetchEmbeddingInStudy(
      @Valid @RequestBody(required = true) EmbeddingFilter embeddingFilter) {

    EmbeddingDTO embeddingDTO =
        EmbeddingMapper.INSTANCE.toEmbeddingDTO(
            embeddingUseCases
                .fetchEmbeddingInStudyUseCase()
                .execute(
                    embeddingFilter.getReductionTechnique(),
                    embeddingFilter.getEmbeddingType(),
                    embeddingFilter.getStudyIds()));
    return ResponseEntity.ok(embeddingDTO);
  }
}
