package org.cbioportal.domain.embedding.usecase;

import org.cbioportal.domain.embedding.repository.EmbeddingRepository;
import org.springframework.stereotype.Service;

@Service
public class FetchEmbeddingInStudyUseCase {
    
    private final EmbeddingRepository embeddingRepository;
    
    public FetchEmbeddingInStudyUseCase(EmbeddingRepository embeddingRepository){
        this.embeddingRepository = embeddingRepository;
    }

    /**
     * Executes the use case to retrieve embedding data based on study and filter criteria.
     *
     * <p>This method passes information from api into the repository layer.
     * 
     *
     *
     * <ul>
     *   <li> 
     *   <li>
     *   <li>
     *   <li>
     * </ul>
     *
     * @param reductionTechnique
     * @param entityType
     * @param studyId
     * @return list of {@link } objects matching the given filter and search criteria
     * @see
     * @see
     */
    public void  execute(String reductionTechnique,String entityType, String studyId  ){
        
    }
    
    
}
