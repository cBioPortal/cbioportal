package org.cbioportal.domain.embedding;

import java.util.List;

public record EmbeddingWithData(EmbeddingDefinition embeddingDefinition,
                                List<EmbeddingData> embeddingData){
    
}
