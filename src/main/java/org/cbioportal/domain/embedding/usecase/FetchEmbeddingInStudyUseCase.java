package org.cbioportal.domain.embedding.usecase;

import java.util.List;
import org.cbioportal.domain.embedding.Embedding;
import org.cbioportal.domain.embedding.EmbeddingData;
import org.cbioportal.domain.embedding.EmbeddingDefinition;
import org.cbioportal.domain.embedding.EmbeddingRow;
import org.cbioportal.domain.embedding.repository.EmbeddingRepository;
import org.cbioportal.domain.embedding.util.EmbeddingUtil;
import org.springframework.stereotype.Service;

/**
 * Use case for retrieving embedding (dimensionality-reduction) data for one or more studies. This
 * class fetches the flat, denormalized rows from the {@link EmbeddingRepository} and reshapes
 * them into the {@link Embedding} aggregate the API layer expects, computing patient/sample
 * counts and deduplicating embedding metadata along the way.
 */
@Service
public class FetchEmbeddingInStudyUseCase {

  private final EmbeddingRepository embeddingRepository;

  public FetchEmbeddingInStudyUseCase(EmbeddingRepository embeddingRepository) {
    this.embeddingRepository = embeddingRepository;
  }

  /**
   * Executes the use case to retrieve embedding data based on study and filter criteria.
   *
   * <p>This method passes the filter criteria into the repository layer to fetch the raw,
   * per-point rows, then uses {@link EmbeddingUtil} to:
   *
   * <ul>
   *   <li>count the distinct patients and samples represented
   *   <li>extract the per-point coordinate/attribute data
   *   <li>collect the distinct study IDs represented
   *   <li>deduplicate the embedding metadata (name, description, entity type, etc.) into a single
   *       {@link EmbeddingDefinition}
   * </ul>
   *
   * @param reductionTechnique the dimensionality-reduction technique to filter by (e.g. "umap",
   *     "pca"), or {@code null} to include all
   * @param entityType the entity type to filter by (e.g. "PATIENT", "SAMPLE"), or {@code null} to
   *     include all
   * @param studyIds the study IDs to fetch embedding data for
   * @return the assembled {@link Embedding} containing per-point data plus study/sample counts
   */
  public Embedding execute(String reductionTechnique, String entityType, List<String> studyIds) {
    List<EmbeddingRow> embeddingRows =
        embeddingRepository.getEmbeddingDataInStudy(reductionTechnique, entityType, studyIds);

    int totalNumOfPatient = EmbeddingUtil.countPatient(embeddingRows);
    int totalNumOfPSample = EmbeddingUtil.countSample(embeddingRows);
    List<EmbeddingData> embeddingData = EmbeddingUtil.getEmbeddingData(embeddingRows);
    List<String> studyIdentifier = EmbeddingUtil.getStudies(embeddingRows);
    List<EmbeddingDefinition> embeddingDefinitions =
        EmbeddingUtil.getUniqueEmbeddingDefinitions(embeddingRows);

    // The filter (reductionTechnique/entityType) is expected to narrow results to a single
    // embedding definition; only the first is used even if the query somehow returns more.
    return new Embedding(
        studyIdentifier,
        totalNumOfPSample,
        totalNumOfPatient,
        embeddingDefinitions.getFirst(),
        embeddingData);
  }
}
