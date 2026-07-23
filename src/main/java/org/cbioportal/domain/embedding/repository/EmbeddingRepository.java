package org.cbioportal.domain.embedding.repository;

import java.util.List;
import org.cbioportal.domain.embedding.EmbeddingRow;

/**
 * Repository interface for accessing embedding data.
 *
 * <p>This abstraction defines the contract for retrieving Embedding data across studies.
 */
public interface EmbeddingRepository {

  List<EmbeddingRow> getEmbeddingDataInStudy(
      String reductionTechnique, String entityType, List<String> studyIds);
}
