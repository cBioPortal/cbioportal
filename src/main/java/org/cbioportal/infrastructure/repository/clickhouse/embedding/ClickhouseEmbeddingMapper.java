package org.cbioportal.infrastructure.repository.clickhouse.embedding;

import java.util.List;
import org.cbioportal.domain.embedding.EmbeddingRow;

public interface ClickhouseEmbeddingMapper {

  List<EmbeddingRow> getEmbeddingDataInStudy(
      String reductionTechnique, String entityType, List<String> studyId);
}
