package org.cbioportal.infrastructure.repository.clickhouse.embedding;

import java.util.List;
import org.cbioportal.domain.embedding.EmbeddingRow;
import org.cbioportal.domain.embedding.repository.EmbeddingRepository;
import org.springframework.stereotype.Repository;

@Repository
public class ClickhouseEmbeddingRepository implements EmbeddingRepository {

  private final ClickhouseEmbeddingMapper mapper;

  public ClickhouseEmbeddingRepository(ClickhouseEmbeddingMapper mapper) {
    this.mapper = mapper;
  }

  /**
   * @param reductionTechnique
   * @param entityType
   * @param studyIds
   * @return
   */
  @Override
  public List<EmbeddingRow> getEmbeddingDataInStudy(
      String reductionTechnique, String entityType, List<String> studyIds) {
    return mapper.getEmbeddingDataInStudy(reductionTechnique, entityType, studyIds);
  }
}
