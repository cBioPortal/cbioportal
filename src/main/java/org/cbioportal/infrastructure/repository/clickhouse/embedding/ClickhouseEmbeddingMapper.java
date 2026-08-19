package org.cbioportal.infrastructure.repository.clickhouse.embedding;

import java.util.List;
import org.cbioportal.domain.embedding.EmbeddingRow;

/**
 * MyBatis mapper backed by {@code ClickhouseEmbeddingMapper.xml}. Joins {@code embedding_data}
 * against {@code cancer_study} and {@code embedding_definition}, so studies or embeddings without a
 * matching row in either table are silently excluded from the result rather than erroring.
 */
public interface ClickhouseEmbeddingMapper {

  /**
   * @param reductionTechnique the dimensionality-reduction technique to filter by, or {@code null}
   *     to include all
   * @param entityType the entity type to filter by, or {@code null} to include all
   * @param studyIds the study IDs to fetch embedding data for
   * @return the matching rows, one per embedded point
   */
  List<EmbeddingRow> getEmbeddingDataInStudy(
      String reductionTechnique, String entityType, List<String> studyIds);
}
