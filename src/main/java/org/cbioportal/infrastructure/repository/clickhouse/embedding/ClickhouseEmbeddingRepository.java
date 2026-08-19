package org.cbioportal.infrastructure.repository.clickhouse.embedding;

import java.util.List;
import org.cbioportal.domain.embedding.EmbeddingRow;
import org.cbioportal.domain.embedding.repository.EmbeddingRepository;
import org.springframework.stereotype.Repository;

/**
 * ClickHouse-backed implementation of {@link EmbeddingRepository}. Delegates to the MyBatis {@link
 * ClickhouseEmbeddingMapper}, which runs the actual SQL against the column-store embedding
 * tables.
 */
@Repository
public class ClickhouseEmbeddingRepository implements EmbeddingRepository {

  private final ClickhouseEmbeddingMapper mapper;

  public ClickhouseEmbeddingRepository(ClickhouseEmbeddingMapper mapper) {
    this.mapper = mapper;
  }

  /**
   * @param reductionTechnique the dimensionality-reduction technique to filter by, or {@code
   *     null} to include all
   * @param entityType the entity type to filter by, or {@code null} to include all
   * @param studyIds the study IDs to fetch embedding data for
   * @return the matching rows, one per embedded point
   */
  @Override
  public List<EmbeddingRow> getEmbeddingDataInStudy(
      String reductionTechnique, String entityType, List<String> studyIds) {
    return mapper.getEmbeddingDataInStudy(reductionTechnique, entityType, studyIds);
  }
}
