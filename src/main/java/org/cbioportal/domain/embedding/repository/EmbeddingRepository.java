package org.cbioportal.domain.embedding.repository;

import java.util.List;
import org.cbioportal.domain.embedding.EmbeddingRow;

/**
 * Repository interface for accessing embedding data.
 *
 * <p>This abstraction defines the contract for retrieving Embedding data across studies.
 */
public interface EmbeddingRepository {

  /**
   * Fetches the raw, per-point embedding rows for the given studies, optionally narrowed by
   * reduction technique and entity type.
   *
   * @param reductionTechnique the dimensionality-reduction technique to filter by (e.g. "umap",
   *     "pca"), or {@code null} to include all
   * @param entityType the entity type to filter by (e.g. "PATIENT", "SAMPLE"), or {@code null} to
   *     include all
   * @param studyIds the study IDs to fetch embedding data for
   * @return the matching rows, one per embedded point
   */
  List<EmbeddingRow> getEmbeddingDataInStudy(
      String reductionTechnique, String entityType, List<String> studyIds);
}
