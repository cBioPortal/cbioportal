package org.cbioportal.infrastructure.repository.clickhouse.embedding;

import org.cbioportal.domain.embedding.EmbeddingWithData;
import org.cbioportal.domain.embedding.repository.EmbeddingRepository;

import java.util.List;

public class ClickhouseEmbeddingRepository implements EmbeddingRepository {
    
    private final ClickhouseEmbeddingMapper mapper;

    public ClickhouseEmbeddingRepository(ClickhouseEmbeddingMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * @param reductionTechnique 
     * @param entityType
     * @param studyId
     * @return
     */
    @Override
    public List<EmbeddingWithData> getEmbeddingDataInStudy(String reductionTechnique, String entityType, String studyId) {
        return mapper.getEmbeddingDataInStudy(
            reductionTechnique, entityType, studyId
        );
    }
}
