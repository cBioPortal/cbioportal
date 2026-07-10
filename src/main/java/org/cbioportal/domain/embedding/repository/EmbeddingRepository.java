package org.cbioportal.domain.embedding.repository;

import org.cbioportal.domain.embedding.EmbeddingWithData;

import java.util.List;

/**
 * Repository interface for accessing embedding data.
 *
 * <p>This abstraction defines the contract for retrieving Embedding data across
 * studies.
 */
public interface EmbeddingRepository {
    
    List<EmbeddingWithData> getEmbeddingDataInStudy(
        String reductionTechnique,
        String entityType,
        String studyId
    );
}
