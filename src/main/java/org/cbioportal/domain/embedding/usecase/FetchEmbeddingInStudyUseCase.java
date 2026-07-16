package org.cbioportal.domain.embedding.usecase;

import java.util.List;
import org.cbioportal.domain.embedding.Embedding;
import org.cbioportal.domain.embedding.EmbeddingData;
import org.cbioportal.domain.embedding.EmbeddingDefinition;
import org.cbioportal.domain.embedding.EmbeddingRow;
import org.cbioportal.domain.embedding.repository.EmbeddingRepository;
import org.cbioportal.domain.embedding.util.EmbeddingUtil;
import org.springframework.stereotype.Service;

@Service
public class FetchEmbeddingInStudyUseCase {

  private final EmbeddingRepository embeddingRepository;

  public FetchEmbeddingInStudyUseCase(EmbeddingRepository embeddingRepository) {
    this.embeddingRepository = embeddingRepository;
  }

  /**
   * Executes the use case to retrieve embedding data based on study and filter criteria.
   *
   * <p>This method passes information from api into the repository layer.
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
  public Embedding execute(String reductionTechnique, String entityType, String studyId) {
    List<EmbeddingRow> embeddingRows =
        embeddingRepository.getEmbeddingDataInStudy(reductionTechnique, entityType, studyId);

    int totalNumOfPatient = EmbeddingUtil.countPatient(embeddingRows);
    int totalNumOfPSample = EmbeddingUtil.countSample(embeddingRows);
    List<EmbeddingData> embeddingData = EmbeddingUtil.getEmbeddingData(embeddingRows);
    List<String> studyIdentifier = EmbeddingUtil.getStudies(embeddingRows);
    List<EmbeddingDefinition> embeddingDefinitions =
        EmbeddingUtil.getUniqueEmbeddingDefinitions(embeddingRows);

    return new Embedding(
        studyIdentifier,
        totalNumOfPSample,
        totalNumOfPatient,
        embeddingDefinitions.getFirst(),
        embeddingData);
  }
}
