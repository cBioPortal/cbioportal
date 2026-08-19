package org.cbioportal.domain.embedding.util;

import java.util.*;
import org.cbioportal.domain.embedding.EmbeddingData;
import org.cbioportal.domain.embedding.EmbeddingDefinition;
import org.cbioportal.domain.embedding.EmbeddingRow;

/**
 * Static helpers for reshaping the flat {@link EmbeddingRow} rows returned by the repository
 * (one row per point, denormalized with study/embedding metadata repeated on every row) into the
 * aggregated shapes {@link org.cbioportal.domain.embedding.usecase.FetchEmbeddingInStudyUseCase}
 * needs.
 */
public abstract class EmbeddingUtil {
  private EmbeddingUtil() {}

  /**
   * Counts the distinct patients represented across the given rows.
   *
   * @param embeddingRows the raw per-point rows
   * @return the number of distinct patient IDs
   */
  public static int countPatient(List<EmbeddingRow> embeddingRows) {
    return Math.toIntExact(embeddingRows.stream().map(EmbeddingRow::patientId).distinct().count());
  }

  /**
   * Counts the distinct samples represented across the given rows. Rows from patient-level
   * embeddings have no {@code sampleId}, so null/empty values are filtered out before counting.
   *
   * @param embeddingRows the raw per-point rows
   * @return the number of distinct sample IDs
   */
  public static int countSample(List<EmbeddingRow> embeddingRows) {
    return Math.toIntExact(
        embeddingRows.stream()
            .map(EmbeddingRow::sampleId)
            .filter(Objects::nonNull)
            .filter(id -> !id.isEmpty())
            .distinct()
            .count());
  }

  /**
   * Extracts the per-point coordinate and custom-attribute data from each row, dropping the
   * study/embedding-definition metadata that's only needed by {@link
   * #getUniqueEmbeddingDefinitions} and {@link #getStudies}.
   *
   * @param embeddingRows the raw per-point rows
   * @return one {@link EmbeddingData} per input row, in the same order
   */
  public static List<EmbeddingData> getEmbeddingData(List<EmbeddingRow> embeddingRows) {
    List<EmbeddingData> embeddingDataList = new ArrayList<>();

    for (EmbeddingRow embeddingRow : embeddingRows) {
      String patientId = embeddingRow.patientId();
      String sampleId = embeddingRow.sampleId();
      Double x = embeddingRow.x();
      Double y = embeddingRow.y();
      String customAttribute = embeddingRow.customAttribute();

      embeddingDataList.add(new EmbeddingData(patientId, sampleId, x, y, customAttribute));
    }
    return embeddingDataList;
  }

  /**
   * @param embeddingRows the raw per-point rows
   * @return the distinct study IDs represented across the rows
   */
  public static List<String> getStudies(List<EmbeddingRow> embeddingRows) {
    return embeddingRows.stream().map(EmbeddingRow::studyIdentifier).distinct().toList();
  }

  /**
   * Deduplicates the embedding-definition metadata (internal ID, description, entity type, short
   * name) that's repeated on every row belonging to the same embedding.
   *
   * <p>TODO check this method could be improved maybe we could use a hashmap to simplify the
   * process
   *
   * @param embeddingRows the raw per-point rows
   * @return the distinct {@link EmbeddingDefinition}s represented across the rows
   */
  public static List<EmbeddingDefinition> getUniqueEmbeddingDefinitions(
      List<EmbeddingRow> embeddingRows) {
    Set<EmbeddingDefinition> definitionSet = new HashSet<>();
    for (EmbeddingRow embeddingRow : embeddingRows) {
      int internalId = embeddingRow.internalId();
      String description = embeddingRow.description();
      String entityType = embeddingRow.entityType();
      String shortName = embeddingRow.shortName();

      definitionSet.add(new EmbeddingDefinition(internalId, description, entityType, shortName));
    }

    return definitionSet.stream().toList();
  }
}
