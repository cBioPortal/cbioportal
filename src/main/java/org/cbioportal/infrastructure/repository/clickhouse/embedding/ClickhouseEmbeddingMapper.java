package org.cbioportal.infrastructure.repository.clickhouse.embedding;

import org.cbioportal.domain.embedding.EmbeddingWithData;

import java.util.List;

public interface ClickhouseEmbeddingMapper {

    List<EmbeddingWithData> getEmbeddingDataInStudy(
        String reductionTechnique,
        String entityType,
        String studyId
    );
}
